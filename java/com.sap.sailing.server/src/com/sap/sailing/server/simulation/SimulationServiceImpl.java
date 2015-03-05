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
import java.util.concurrent.FutureTask;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathType;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.Simulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulationResults;
import com.sap.sailing.simulator.impl.MaximumTurnTimes;
import com.sap.sailing.simulator.impl.PolarDiagramGPS;
import com.sap.sailing.simulator.impl.SimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.impl.SparsePolarDataException;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldTrackedRaceImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.util.SmartFutureCache;
import com.sap.sse.util.SmartFutureCache.EmptyUpdateInterval;

public class SimulationServiceImpl implements SimulationService {

    final private Executor executor;
    final private SmartFutureCache<LegIdentifier, SimulationResults, SmartFutureCache.EmptyUpdateInterval> cache;
    final private RacingEventService racingEventService;

    public SimulationServiceImpl(Executor executor, RacingEventService racingEventService) {
        this.executor = executor;
        this.racingEventService = racingEventService;
        if (racingEventService != null) {
            this.cache = new SmartFutureCache<LegIdentifier, SimulationResults, SmartFutureCache.EmptyUpdateInterval>(
                    new SmartFutureCache.AbstractCacheUpdater<LegIdentifier, SimulationResults, SmartFutureCache.EmptyUpdateInterval>() {
                        @Override
                        public SimulationResults computeCacheUpdate(LegIdentifier key, EmptyUpdateInterval updateInterval) throws Exception {
                            return computeSimulationResults(key);
                        }
                    }, "SmartFutureCache.simulationService (" + racingEventService.toString() + ")");
        } else {
            this.cache = null;
        }
    }

    private class Listener extends AbstractRaceChangeListener {
        private final LegIdentifier legIdentifier;

        public Listener(LegIdentifier legIdentifier) {
            this.legIdentifier = legIdentifier;
        }

        @Override
        protected void defaultAction() {
            cache.triggerUpdate(legIdentifier, null);
        }

        // simulation is not influenced by live-delay
        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
        }

    }

    @Override
    public SimulationResults getSimulationResults(LegIdentifier legIdentifier) {
        SimulationResults result = cache.get(legIdentifier, false);
        if (result == null) {
            TrackedRace trackedRace = racingEventService.getTrackedRace(legIdentifier);
            trackedRace.addListener(new Listener(legIdentifier));
            cache.triggerUpdate(legIdentifier, null);
            result = cache.get(legIdentifier, true); // take first simulation result that becomes available
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
            markPassing = trackedRace.getMarkPassingsInOrder(fromWaypoint).iterator().next();
            if (markPassing != null) {
                startTimePoint = markPassing.getTimePoint();
            }
            Iterator<MarkPassing> markPassingIterator = trackedRace.getMarkPassingsInOrder(toWaypoint).iterator();
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
                // TODO Auto-generated catch block
                e.printStackTrace();
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
