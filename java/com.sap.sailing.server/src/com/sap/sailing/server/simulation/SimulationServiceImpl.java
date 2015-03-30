package com.sap.sailing.server.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.LegIdentifierImpl;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathType;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulationResults;
import com.sap.sailing.simulator.Simulator;
import com.sap.sailing.simulator.impl.MaximumTurnTimes;
import com.sap.sailing.simulator.impl.PolarDiagramGPS;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.SimulatorImpl;
import com.sap.sailing.simulator.impl.SparsePolarDataException;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldTrackedRaceImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.SmartFutureCache;

public class SimulationServiceImpl implements SimulationService {

    private static final Logger logger = Logger.getLogger(SimulationService.class.getName());

    final private Executor executor;
    final private SmartFutureCache<LegIdentifier, SimulationResults, SmartFutureCache.EmptyUpdateInterval> cache;
    final private RacingEventService racingEventService;
    final private ScheduledExecutorService scheduler;
    final private HashMap<String, SimulationRaceListener> raceListeners;
    final private HashMap<RaceIdentifier, LegChangeListener> legListeners;
    final private long WAIT_MILLIS = 20000; // milliseconds to wait until earliest cache-update for simulation
    
    public SimulationServiceImpl(Executor executor, RacingEventService racingEventService) {
        this.executor = executor;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.racingEventService = racingEventService;
        if (racingEventService != null) {
            this.raceListeners = new HashMap<String, SimulationRaceListener>();
            this.legListeners = new HashMap<RaceIdentifier, LegChangeListener>();
            this.cache = new SmartFutureCache<LegIdentifier, SimulationResults, SmartFutureCache.EmptyUpdateInterval>(
                    new SmartFutureCache.AbstractCacheUpdater<LegIdentifier, SimulationResults, SmartFutureCache.EmptyUpdateInterval>() {
                        @Override
                        public SimulationResults computeCacheUpdate(LegIdentifier key, SmartFutureCache.EmptyUpdateInterval updateInterval) throws Exception {
                            logger.info("Simulation Started: \"" + key.toString() + "\"");
                            SimulationResults results = computeSimulationResults(key);
                            logger.info("Simulation Finished: \"" + key.toString() + "\", Results-Version: "+ (results==null?0:results.hashCode()));
                            return results;
                        }
                    }, "SmartFutureCache.simulationService (" + racingEventService.toString() + ")");
        } else {
            this.raceListeners = null;
            this.legListeners = null;
            this.cache = null;
        }
    }

    private class SimulationRaceListener implements RaceListener {
        @Override
        public void raceAdded(TrackedRace trackedRace) {
        }

        @Override
        public void raceRemoved(TrackedRace trackedRace) {
            RaceIdentifier raceIdentifier = trackedRace.getRaceIdentifier();
            LegChangeListener listener = legListeners.get(raceIdentifier); 
            if (listener != null) {
                trackedRace.removeListener(listener);
            }
            int legNumber = 1;
            Iterator<TrackedLeg> iterator = trackedRace.getTrackedLegs().iterator(); 
            while (iterator.hasNext()) {
                LegIdentifier key = new LegIdentifierImpl((RegattaAndRaceIdentifier)raceIdentifier, ""+legNumber);
                SimulationResults entry = cache.get(key, false);
                if (entry != null) {
                    cache.remove(key);
                }
                legNumber++;
                iterator.next();
            }
            legListeners.remove(raceIdentifier);
        }
    }
    
    private class LegChangeListener extends AbstractRaceChangeListener {
        private final TrackedRace trackedRace;
        private LegIdentifier legIdentifier;
        private final ScheduledExecutorService scheduler;
        private boolean covered;
        

        public LegChangeListener(TrackedRace trackedRace, ScheduledExecutorService scheduler) {
            this.trackedRace = trackedRace;
            this.legIdentifier = null;
            this.scheduler = scheduler;
            this.covered = false;
        }

        public boolean isLive() {
            return trackedRace.isLive(MillisecondsTimePoint.now());
        }
        
        public void setLegIdentifier(LegIdentifier legIdentifier) {
            if (this.legIdentifier != null) {
                if (!this.legIdentifier.equals(legIdentifier)) {
                    this.legIdentifier = legIdentifier;
                    this.covered = false;
                }
            } else {
                this.legIdentifier = legIdentifier;
            }
        }
        
        @Override
        protected void defaultAction() {
            if ((!this.covered)&&(legIdentifier!=null)) {
                this.covered = true;
                LegIdentifier tmpLegIdentifier = new LegIdentifierImpl(legIdentifier, legIdentifier.getLegName());
                scheduler.schedule(() -> triggerUpdate(tmpLegIdentifier), WAIT_MILLIS, TimeUnit.MILLISECONDS);
            }
        }

        private void triggerUpdate(LegIdentifier legIdentifier) {
            if (this.legIdentifier.equals(legIdentifier)) {
                this.covered = false;
            }
            logger.info("Simulation Scheduled Update Triggered: \"" + legIdentifier.toString() + "\"");
            cache.triggerUpdate(legIdentifier, null);
        }
        

        @Override
        public void startOfRaceChanged(TimePoint oldStartOfRace, TimePoint newStartOfRace) {
            // relevant for simulation: start of leg 1 changes; requires update of leg 1
            // TODO: update leg 1
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            // relevant for simulation: update all legs when wind changes overall
            // TODO: update all legs
        }

        @Override
        public void startOfTrackingChanged(TimePoint startOfTracking) {
            // irrelevant for simulation
        }

        @Override
        public void endOfTrackingChanged(TimePoint endOfTracking) {
            // irrelevant for simulation
        }

        @Override
        public void startTimeReceivedChanged(TimePoint startTimeReceived) {
            // irrelevant for simulation: should be covered by startOfRaceChanged()
        }

        @Override
        public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack) {
            // relevant for simulation
            // TODO: identify influenced legs and update these legs
        }

        @Override
        public void windDataReceived(Wind wind, WindSource windSource) {
            // relevant for simulation: update legs influenced by wind
            // TODO: update influenced legs; "live" update current leg
            defaultAction();
        }

        @Override
        public void windDataRemoved(Wind wind, WindSource windSource) {
            // relevant for simulation: update legs influenced by wind
            // TODO: update influenced legs; "live" update current leg
            defaultAction();
        }

        @Override
        public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            // relevant for simulation: update legs influenced by wind
            // TODO: update influenced legs; "live" update current leg
            defaultAction();
        }

        @Override
        public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
            // irrelevant for simulation: covered by wind estimation
        }

        @Override
        public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings, Iterable<MarkPassing> markPassings) {
            // relevant for simulation: update start- and end-times of legs
            // TODO: update influenced legs; "live" update current leg
        }

        @Override
        public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
            // irrelevant for simulation
        }

        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
            // irrelevant for simulation
        }

        @Override
        public void waypointAdded(int zeroBasedIndex, Waypoint waypointThatGotAdded) {
            // relevant for simulation: update legs influenced waypoint
            // TODO: update influenced legs; "live" update current leg
        }

        @Override
        public void waypointRemoved(int zeroBasedIndex, Waypoint waypointThatGotRemoved) {
            // relevant for simulation: update legs influenced waypoint
            // TODO: update influenced legs; "live" update current leg
        }

    }

    @Override
    public int getSimulationResultsVersion(LegIdentifier legIdentifier) {
        SimulationResults result = cache.get(legIdentifier, false);
        int version = (result == null ? 0 : result.hashCode());
        logger.fine("Simulation Results-Version: " + + version);
        return version;
    }

    @Override
    public SimulationResults getSimulationResults(LegIdentifier legIdentifier) {
        SimulationResults result = cache.get(legIdentifier, false);
        if (result == null) {
            logger.fine("Simulation Get: Cache Empty: \"" + legIdentifier.toString() + "\"");
            if (!raceListeners.containsKey(legIdentifier.getRegattaName())) {
                Regatta regatta = racingEventService.getRegattaByName(legIdentifier.getRegattaName());
                DynamicTrackedRegatta trackedRegatta = racingEventService.getTrackedRegatta(regatta);
                SimulationRaceListener raceListener = new SimulationRaceListener(); 
                raceListeners.put(legIdentifier.getRegattaName(), raceListener);
                trackedRegatta.addRaceListener(raceListener);
            }
            if (!legListeners.containsKey(legIdentifier.getRaceIdentifier())) {
                TrackedRace trackedRace = racingEventService.getTrackedRace(legIdentifier);
                if (trackedRace != null) {
                    LegChangeListener listener = new LegChangeListener(trackedRace, scheduler);
                    if (listener.isLive()) {
                        listener.setLegIdentifier(legIdentifier);
                    }
                    legListeners.put(legIdentifier.getRaceIdentifier(), listener);
                    trackedRace.addListener(listener);
                }
            } else {
                LegChangeListener listener = legListeners.get(legIdentifier.getRaceIdentifier());
                if (listener.isLive()) {
                    listener.setLegIdentifier(legIdentifier);
                }
            }
            logger.info("Simulation Get: Update Triggered: \"" + legIdentifier.toString() + "\"");
            cache.triggerUpdate(legIdentifier, null);
            result = cache.get(legIdentifier, true); // take first simulation result that becomes available
        }
        if (result == null) {
            logger.fine("Simulation Get: Null-Result: \"" + legIdentifier.toString() + "\"");
        }
        return result;
    }

    public SimulationResults computeSimulationResults(LegIdentifier legIdentifier) throws InterruptedException,
            ExecutionException {
        SimulationResults result = null;
        TrackedRace trackedRace = racingEventService.getTrackedRace(legIdentifier);
        if (trackedRace != null) {
            Leg leg = trackedRace.getRace().getCourse().getLegs().get(legIdentifier.getLegNumber());
            // get previous mark or start line as start-position
            Waypoint fromWaypoint = leg.getFrom();
            // get next mark as end-position
            Waypoint toWaypoint = leg.getTo();

            TimePoint startTimePoint = null;
            TimePoint endTimePoint = null;
            MarkPassing markPassing;
            Iterator<MarkPassing> markPassingIterator = trackedRace.getMarkPassingsInOrder(fromWaypoint).iterator();
            if (markPassingIterator.hasNext()) {
                markPassing = markPassingIterator.next();
            } else {
                markPassing = null;
            }
            if (markPassing != null) {
                startTimePoint = markPassing.getTimePoint();
            }
            markPassingIterator = trackedRace.getMarkPassingsInOrder(toWaypoint).iterator();
            if (markPassingIterator.hasNext()) {
                markPassing = markPassingIterator.next();
            } else {
                markPassing = null;
            }
            if (markPassing != null) {
                endTimePoint = markPassing.getTimePoint();
            }
            long legDuration = 0;
            if ((startTimePoint != null) && (endTimePoint != null)) {
                legDuration = endTimePoint.asMillis() - startTimePoint.asMillis();
            }
            Position startPosition = null;
            Position endPosition = null;
            if (startTimePoint != null) {
                startPosition = trackedRace.getApproximatePosition(fromWaypoint, startTimePoint);
            }
            if (endTimePoint != null) {
                endPosition = trackedRace.getApproximatePosition(toWaypoint, endTimePoint);
            } else if (startTimePoint != null) {
                endPosition = trackedRace.getApproximatePosition(toWaypoint, startTimePoint);
            }

            // determine legtype upwind/downwind/reaching
            LegType legType = null;
            try {
                legType = trackedRace.getTrackedLeg(leg).getLegType(startTimePoint);
            } catch (NoWindException e) {
                return null;
            }

            // get windfield
            WindFieldGenerator windField = new WindFieldTrackedRaceImpl(trackedRace);
            Duration timeStep = new MillisecondsDurationImpl(15 * 1000);
            windField.generate(startTimePoint, null, timeStep);

            // prepare simulation-parameters
            List<Position> course = new ArrayList<Position>();
            course.add(startPosition);
            course.add(endPosition);
            BoatClass boatClass = trackedRace.getRace().getBoatClass();
            PolarDataService polarDataService = racingEventService.getPolarDataService();
            PolarDiagram polarDiagram;
            try {
                polarDiagram = new PolarDiagramGPS(boatClass, polarDataService);
            } catch (SparsePolarDataException e) {
                polarDiagram = null;
                // TODO: raise a UI message, to inform user about missing polar data resulting in unability to simulate
            }
            double simuStepSeconds = startPosition.getDistance(endPosition).getNauticalMiles()
                    / ((PolarDiagramGPS) polarDiagram).getAvgSpeed() * 3600 / 100;
            Duration simuStep = new MillisecondsDurationImpl(Math.round(simuStepSeconds) * 1000);
            SimulationParameters simulationPars = new SimulationParametersImpl(course, polarDiagram, windField,
                    simuStep, SailingSimulatorConstants.ModeEvent, true, true, legType);
            Map<PathType, Path> paths = null;
            if ((polarDiagram != null)&&(legType != LegType.REACHING)) {
                paths = getAllPathsEvenTimed(simulationPars, timeStep.asMillis());
            }
            // prepare simulator-results
            result = new SimulationResults(startTimePoint.asDate(), timeStep.asMillis(), legDuration, startPosition,
                    endPosition, paths, null);
        }
        return result;
    }

    public Map<PathType, Path> getAllPaths(SimulationParameters simulationParameters) throws InterruptedException,
            ExecutionException {

        Simulator simulator = new SimulatorImpl(simulationParameters);
        Map<PathType, Path> result = new HashMap<PathType, Path>();

        FutureTask<Path> taskOmniscient = null;
        if (simulationParameters.showOmniscient()) {
            // schedule omniscient task
            taskOmniscient = new FutureTask<Path>(() -> simulator.getPath(PathType.OMNISCIENT, null));
            executor.execute(taskOmniscient);
        }

        // schedule 1-turner tasks
        FutureTask<Path> task1TurnerLeft = new FutureTask<Path>(() -> simulator.getPath(PathType.ONE_TURNER_LEFT, null));
        FutureTask<Path> task1TurnerRight = new FutureTask<Path>(() -> simulator.getPath(PathType.ONE_TURNER_RIGHT, null));
        executor.execute(task1TurnerLeft);
        executor.execute(task1TurnerRight);

        // collect 1-turner results
        result.put(PathType.ONE_TURNER_LEFT, task1TurnerLeft.get());
        result.put(PathType.ONE_TURNER_RIGHT, task1TurnerRight.get());

        FutureTask<Path> taskOpportunistLeft = null;
        FutureTask<Path> taskOpportunistRight = null;
        if (simulationParameters.showOpportunist()) {        
            // maximum turn times
            MaximumTurnTimes maxTurnTimes = new MaximumTurnTimes(task1TurnerLeft.get().getMaxTurnTime(), task1TurnerRight.get().getMaxTurnTime());

            // schedule opportunist tasks (which depend on 1-turner results)
            taskOpportunistLeft = new FutureTask<Path>(() -> simulator.getPath(PathType.OPPORTUNIST_LEFT, maxTurnTimes));
            taskOpportunistRight = new FutureTask<Path>(() -> simulator.getPath(PathType.OPPORTUNIST_RIGHT, maxTurnTimes));
            executor.execute(taskOpportunistLeft);
            executor.execute(taskOpportunistRight);

            // collect opportunist results
            Path pathOpportunistLeft = taskOpportunistLeft.get();
            if (pathOpportunistLeft.getTurnCount() == 1) {
                pathOpportunistLeft = result.get(PathType.ONE_TURNER_LEFT);
            }
            result.put(PathType.OPPORTUNIST_LEFT, pathOpportunistLeft);
            Path pathOpportunistRight = taskOpportunistRight.get();
            if (pathOpportunistRight.getTurnCount() == 1) {
                pathOpportunistRight = result.get(PathType.ONE_TURNER_RIGHT);
            }
            result.put(PathType.OPPORTUNIST_RIGHT, pathOpportunistRight);
        }

        if (simulationParameters.showOmniscient()) {
            // collect omniscient result (last, since usually slowest calculation)
            result.put(PathType.OMNISCIENT, taskOmniscient.get());
        }
        // return combined result
        return result;
    }

    public Map<PathType, Path> getAllPathsEvenTimed(SimulationParameters simuPars, long millisecondsStep)
            throws InterruptedException, ExecutionException {
        Map<PathType, Path> allTimedPaths = new TreeMap<PathType, Path>();
        Map<PathType, Path> allPaths = this.getAllPaths(simuPars);
        for (Entry<PathType, Path> entry : allPaths.entrySet()) {
            PathType pathType = entry.getKey();
            Path pathValue = entry.getValue();
            allTimedPaths.put(pathType, pathValue.getEvenTimedPath(millisecondsStep));
        }
        return allTimedPaths;
    }

}
