package com.sap.sailing.server.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.LegIdentifierImpl;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.PathType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulationResults;
import com.sap.sailing.simulator.Simulator;
import com.sap.sailing.simulator.impl.PolarDiagramGPS;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.SimulatorImpl;
import com.sap.sailing.simulator.impl.SparseSimulationDataException;
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

    final private ScheduledExecutorService executor;
    final private SmartFutureCache<LegIdentifier, SimulationResults, SmartFutureCache.EmptyUpdateInterval> cache;
    final private RacingEventService racingEventService;
    final private ScheduledExecutorService scheduler;
    final private HashMap<String, SimulationRaceListener> raceListeners;
    final private HashMap<RaceIdentifier, LegChangeListener> legListeners;
    final private long WAIT_MILLIS = 20000; // milliseconds to wait until earliest cache-update for simulation
    
    public SimulationServiceImpl(ScheduledExecutorService executor, RacingEventService racingEventService) {
        this.executor = executor;
        this.scheduler = executor;
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
                            logger.info("Simulation Finished: \"" + key.toString() + "\", Results-Version: "+ (results==null?0:results.getVersion().asMillis()));
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
    
    /**
     * A stateful listener whose {@link #legIdentifier} may change over time, updated to the most recent request. When
     * changes to the race are received that are considered relevant for the simulation results, a cache update will be
     * triggered {@link SimulationServiceImpl#WAIT_MILLIS} milliseconds after the change event. To avoid redundant
     * triggers, more updates received before the wait period expires are ignored as long as they are for the same leg.<p>
     * 
     * TODO the {@link #covered} and {@link #legIdentifier} fields with the stateful design and the dependence on {@link #isLive}
     * seem a bit "smelly." The {@link #legIdentifier} field is updated only in "live" mode when a simulator result is requested.
     * It therefore remains at the last leg for which a result was requested while the race was in live mode. This doesn't
     * necessarily have to be the race's last leg. When updates strike---such as a mark moving---then the simulation results
     * for all of the race's legs at least need to be invalidated. The way the implementation looks right now it seems that
     * results for legs that were requested earlier will not be updated because {@link #legIdentifier} does not point to them.
     * And since {@link #legIdentifier} is no more updated for non-live races, updates to older leg simulation results will
     * never happen...
     */
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
            if ((!this.covered) && (legIdentifier != null)) {
                this.covered = true;
                LegIdentifier tmpLegIdentifier = new LegIdentifierImpl(legIdentifier.getRaceIdentifier(), legIdentifier.getLegName());
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
        public void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime) {
            // relevant for simulation? for last leg?
            // TODO: update last leg?
        }

        @Override
        public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
            // relevant for simulation: update all legs when wind changes overall
            // TODO: update all legs
        }

        @Override
        public void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking) {
            // irrelevant for simulation
        }

        @Override
        public void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking) {
            // irrelevant for simulation
        }

        @Override
        public void startTimeReceivedChanged(TimePoint startTimeReceived) {
            // irrelevant for simulation: should be covered by startOfRaceChanged()
        }

        @Override
        public void markPositionChanged(GPSFix fix, Mark mark, boolean firstInTrack) {
            // relevant for simulation
            if (this.isLive()) {
                defaultAction();
            }
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
    public long getSimulationResultsVersion(LegIdentifier legIdentifier) {
        SimulationResults result = cache.get(legIdentifier, false);
        long version = (result == null ? 0 : result.getVersion().asMillis());
        logger.fine("Simulation Results-Version: " + version);
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
                trackedRegatta.addRaceListener(raceListener, /* Not replicated */ Optional.empty(), /* synchronous */ false);
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

    private List<Position> getLinePositions(Waypoint wayPoint, TimePoint at, TrackedRace trackedRace) {
        List<Position> line = new ArrayList<Position>();
        if (wayPoint != null) {
            for (Mark lineMark : wayPoint.getMarks()) {
                Position estimatedMarkPosition = trackedRace.getOrCreateTrack(lineMark).getEstimatedPosition(at, /* extrapolate */ false);
                if (estimatedMarkPosition != null) {
                    line.add(estimatedMarkPosition);
                }
            }
        }
        return line;
    }

    public SimulationResults computeSimulationResults(LegIdentifier legIdentifier) throws InterruptedException,
            ExecutionException {
        TimePoint simulationStartTime = MillisecondsTimePoint.now();
        SimulationResults result = null;
        TrackedRace trackedRace = racingEventService.getTrackedRace(legIdentifier);
        if (trackedRace != null) {
            boolean isLive = trackedRace.isLive(simulationStartTime);
            int legNumber = legIdentifier.getLegNumber();
            Course raceCourse = trackedRace.getRace().getCourse();
            Leg leg = raceCourse.getLegs().get(legNumber);
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
            } else if (isLive && (legNumber == 0)) {
                startTimePoint = simulationStartTime;
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
            if (isLive && (markPassing == null)) {
                endTimePoint = simulationStartTime;
            }
            Position startPosition = null;
            List<Position> startLine = null;
            Position endPosition = null;
            List<Position> endLine = null;
            if (startTimePoint != null) {
                startPosition = trackedRace.getApproximatePosition(fromWaypoint, startTimePoint);
                List<Position> line = this.getLinePositions(fromWaypoint, startTimePoint, trackedRace);
                if (line.size() == 2) {
                    startLine = line;
                }
            }
            if (endTimePoint != null) {
                endPosition = trackedRace.getApproximatePosition(toWaypoint, endTimePoint);
                List<Position> line = this.getLinePositions(toWaypoint, endTimePoint, trackedRace);
                if (line.size() == 2) {
                    endLine = line;
                }
            } else if (startTimePoint != null) {
                endPosition = trackedRace.getApproximatePosition(toWaypoint, startTimePoint);
            }

            // determine legtype upwind/downwind/reaching
            LegType legType = null;
            if (startTimePoint != null) {
                try {
                    legType = trackedRace.getTrackedLeg(leg).getLegType(startTimePoint);
                } catch (NoWindException e) {
                    return null;
                }
            } else {
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
            } catch (SparseSimulationDataException e) {
                polarDiagram = null;
                // TODO: raise a UI message, to inform user about missing polar data resulting in unability to simulate
            }
            Map<PathType, Path> paths = null;
            if (polarDiagram != null) {
                double simuStepSeconds = startPosition.getDistance(endPosition).getNauticalMiles()
                        / ((PolarDiagramGPS) polarDiagram).getAvgSpeed() * 3600 / 100;
                Duration simuStep = new MillisecondsDurationImpl(Math.round(simuStepSeconds) * 1000);
                SimulationParameters simulationPars = new SimulationParametersImpl(course, startLine, endLine, polarDiagram,
                        windField, simuStep, SailingSimulatorConstants.ModeEvent, true, true, legType);
                paths = getAllPathsEvenTimed(simulationPars, timeStep.asMillis());
            }
            // prepare simulator-results
            result = new SimulationResults(startTimePoint.asDate(), timeStep.asMillis(), legDuration, startPosition,
                    endPosition, paths, null, simulationStartTime);
        }
        return result;
    }

    public Map<PathType, Path> getAllPaths(SimulationParameters simulationParameters) throws InterruptedException,
            ExecutionException {

        Simulator simulator = new SimulatorImpl(simulationParameters);
        Map<PathType, Path> result = new HashMap<PathType, Path>();

        Future<Path> taskOmniscient = null;
        if (simulationParameters.showOmniscient()) {
            // schedule omniscient task
            taskOmniscient = executor.submit(() -> simulator.getPath(PathType.OMNISCIENT));
        }

        Future<Path> task1TurnerLeft = null;
        Future<Path> task1TurnerRight = null;
        if (simulationParameters.getLegType() != LegType.REACHING) {
            // schedule 1-turner tasks
            task1TurnerLeft = executor.submit(() -> simulator.getPath(PathType.ONE_TURNER_LEFT));
            task1TurnerRight = executor.submit(() -> simulator.getPath(PathType.ONE_TURNER_RIGHT));
        }

        Future<Path> taskOpportunistLeft = null;
        Future<Path> taskOpportunistRight = null;
        if (simulationParameters.showOpportunist()) {        
            // schedule opportunist tasks (which depend on 1-turner results)
            taskOpportunistLeft = executor.submit(() -> simulator.getPath(PathType.OPPORTUNIST_LEFT));
            taskOpportunistRight = executor.submit(() -> simulator.getPath(PathType.OPPORTUNIST_RIGHT));
        }

        Path path1TurnerLeft = null;
        Path path1TurnerRight = null;
        if (simulationParameters.getLegType() != LegType.REACHING) {
            // collect 1-turner results
            path1TurnerLeft = task1TurnerLeft.get();
            result.put(PathType.ONE_TURNER_LEFT, path1TurnerLeft);
            path1TurnerRight = task1TurnerRight.get();
            result.put(PathType.ONE_TURNER_RIGHT, path1TurnerRight);
        }
        
        Path pathOpportunistLeft = null;
        Path pathOpportunistRight = null;
        if (simulationParameters.showOpportunist()) {
            // collect opportunist results
            pathOpportunistLeft = taskOpportunistLeft.get();
            if (path1TurnerLeft != null) {
                if (!path1TurnerLeft.getAlgorithmTimedOut() && (pathOpportunistLeft.getTurnCount() == 1)) {
                    pathOpportunistLeft = path1TurnerLeft;
                }
            }
            result.put(PathType.OPPORTUNIST_LEFT, pathOpportunistLeft);
            pathOpportunistRight = taskOpportunistRight.get();
            if (path1TurnerRight != null) {
                if (!path1TurnerRight.getAlgorithmTimedOut() && (pathOpportunistRight.getTurnCount() == 1)) {
                    pathOpportunistRight = path1TurnerRight;
                }
            }
            result.put(PathType.OPPORTUNIST_RIGHT, pathOpportunistRight);
        }

        if (simulationParameters.showOmniscient()) {
            // collect omniscient result (last, since usually slowest calculation)
            Path pathOmniscient = taskOmniscient.get();
            if (path1TurnerLeft != null) {
                if (!path1TurnerLeft.getAlgorithmTimedOut() && (pathOmniscient.getFinalTime().after(path1TurnerLeft.getFinalTime()))) {
                    pathOmniscient = path1TurnerLeft;
                }
            }
            if (path1TurnerRight != null) {
                if (!path1TurnerRight.getAlgorithmTimedOut() && (pathOmniscient.getFinalTime().after(path1TurnerRight.getFinalTime()))) {
                    pathOmniscient = path1TurnerRight;
                }
            }
            if (pathOpportunistLeft != null) {
                if (!pathOpportunistLeft.getAlgorithmTimedOut() && (pathOmniscient.getFinalTime().after(pathOpportunistLeft.getFinalTime()))) {
                    pathOmniscient = pathOpportunistLeft;
                }
            }
            if (pathOpportunistRight != null) {
                if (!pathOpportunistRight.getAlgorithmTimedOut() && (pathOmniscient.getFinalTime().after(pathOpportunistRight.getFinalTime()))) {
                    pathOmniscient = pathOpportunistRight;
                }
            }
            result.put(PathType.OMNISCIENT, pathOmniscient);
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
            if (pathValue != null) {
                allTimedPaths.put(pathType, pathValue.getEvenTimedPath(millisecondsStep));
            }
        }
        return allTimedPaths;
    }

}
