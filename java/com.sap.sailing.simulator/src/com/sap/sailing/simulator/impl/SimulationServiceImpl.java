package com.sap.sailing.simulator.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PathType;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.SimulationService;

public class SimulationServiceImpl implements SimulationService {

    private Executor executor;
    private SailingSimulator simulator;

    public SimulationServiceImpl(Executor executor) {
        this.executor = executor;
        // TODO: add smart-future-cache for simulation
    }

    public Map<PathType, Path> getAllPaths(SimulationParameters simuPars) {
        
        // TODO: check smart-future-cache
        
        this.simulator = new SailingSimulatorImpl(simuPars);
        Map<PathType, Path> result = new HashMap<PathType, Path>();
        // schedule omniscient task
        FutureTask<Path> taskOmniscient = new FutureTask<Path>(() -> simulator.getPath(PathType.OMNISCIENT));
        executor.execute(taskOmniscient);

        // schedule 1-turner tasks
        FutureTask<Path> task1TurnerLeft = new FutureTask<Path>(() -> simulator.getPath(PathType.ONE_TURNER_LEFT));
        FutureTask<Path> task1TurnerRight = new FutureTask<Path>(() -> simulator.getPath(PathType.ONE_TURNER_RIGHT));
        executor.execute(task1TurnerLeft);
        executor.execute(task1TurnerRight);

        // collect 1-turner results
        try {
            result.put(PathType.ONE_TURNER_LEFT, task1TurnerLeft.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        try {
            result.put(PathType.ONE_TURNER_RIGHT, task1TurnerRight.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // schedule opportunist tasks (which depend on 1-turner results)
        FutureTask<Path> taskOpportunistLeft = new FutureTask<Path>(() -> simulator.getPath(PathType.OPPORTUNIST_LEFT));
        FutureTask<Path> taskOpportunistRight = new FutureTask<Path>(
                () -> simulator.getPath(PathType.OPPORTUNIST_RIGHT));
        executor.execute(taskOpportunistLeft);
        executor.execute(taskOpportunistRight);

        // collect opportunist results
        try {
            result.put(PathType.OPPORTUNIST_LEFT, taskOpportunistLeft.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        try {
            result.put(PathType.OPPORTUNIST_RIGHT, taskOpportunistRight.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // collect omniscient result (last, since usually slowest calculation)
        try {
            result.put(PathType.OMNISCIENT, taskOmniscient.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // TODO: update smart-future-cache
        
        // return combined result
        return result;
    }

    @Override
    public Map<PathType, Path> getAllPathsEvenTimed(SimulationParameters simuPars, long millisecondsStep) {
        Map<PathType, Path> allTimedPaths = new HashMap<PathType, Path>();
        Map<PathType, Path> allPaths = this.getAllPaths(simuPars);
        for (Entry<PathType, Path> entry : allPaths.entrySet()) {
            PathType pathType = entry.getKey();
            Path pathValue = entry.getValue();
            allTimedPaths.put(pathType, pathValue.getEvenTimedPath(millisecondsStep));
        }
        return allTimedPaths;
    }

}
