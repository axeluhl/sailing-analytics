package com.sap.sse.datamining.impl.components.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Util;
import com.sap.sse.datamining.components.management.DataMiningQueryManager;
import com.sap.sse.datamining.components.management.MemoryInfoProvider;
import com.sap.sse.datamining.components.management.MemoryMonitor;
import com.sap.sse.datamining.components.management.MemoryMonitorAction;

public class QueryManagerMemoryMonitor implements MemoryMonitor {

    private static final String MEMORY_STATUS_LOG_PREFIX = "Memory Status: ";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final MemoryInfoProvider infoProvider;
    private final DataMiningQueryManager queryManager;
    private final List<MemoryMonitorAction> actions;
    private final Timer timer;
    private TimerTask timerTask;
    private final long periodInMs;
    private boolean isPaused;

    public QueryManagerMemoryMonitor(MemoryInfoProvider infoProvider, DataMiningQueryManager queryManager, Iterable<? extends MemoryMonitorAction> actions, long memoryCheckPeriod, TimeUnit unit) {
        this.infoProvider = infoProvider;
        this.queryManager = queryManager;
        this.actions = new ArrayList<>();
        Util.addAll(actions, this.actions);
        Collections.sort(this.actions);
        
        isPaused = false;
        periodInMs = unit.toMillis(memoryCheckPeriod);
        timer = new Timer(this.getClass().getSimpleName() + " Daemon", true);
        timerTask = createTimerTask();
        timer.schedule(timerTask, 0, periodInMs);
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                checkMemory();
            }
        };
    }
    
    private void checkMemory() {
        final long freeMemory = infoProvider.freeMemory();
        final long totalMemory = infoProvider.totalMemory();
        final double freeMemoryRatio = (double) freeMemory / totalMemory;
        final int numberOfRunningQueries = queryManager.getNumberOfRunningQueries();
        
        if (numberOfRunningQueries > 0) {
            logStatus(freeMemory, totalMemory, freeMemoryRatio, numberOfRunningQueries);
        }
        
        boolean actionHasBeenPerformed = false;
        final Iterator<MemoryMonitorAction> actionsIterator = actions.iterator();
        while (!actionHasBeenPerformed && actionsIterator.hasNext()) {
            MemoryMonitorAction action = actionsIterator.next();
            actionHasBeenPerformed = action.checkMemoryAndPerformAction(freeMemoryRatio);
            if (actionHasBeenPerformed) {
                //Also perform actions, that are equally important
                while (actionsIterator.hasNext()) {
                    MemoryMonitorAction nextAction = actionsIterator.next();
                    if (action.compareTo(nextAction) != 0) {
                        break;
                    }
                    nextAction.checkMemoryAndPerformAction(freeMemoryRatio);
                }
            }
        }
    }
    
    private void logStatus(long freeMemory, long totalMemory, double freeMemoryRatio, int numberOfRunningQueries) {
        double freeMemoryMB = (double) freeMemory / 1024 / 1024;
        double totalMemoryMB = (double) totalMemory / 1024 / 1024;
        logInfo(MEMORY_STATUS_LOG_PREFIX + "Free " + String.format("%1$,.2f", freeMemoryMB) + " MB, Total " +
                String.format("%1$,.2f", totalMemoryMB) + " MB, Free in Percent " +
                String.format("%1$,.2f", freeMemoryRatio * 100) + "% with " + numberOfRunningQueries + " queries running.");
    }

    @Override
    public void log(Level level, String message) {
        logger.log(level, message);
    }

    @Override
    public void logInfo(String message) {
        log(Level.INFO, message);
    }

    @Override
    public void logWarning(String message) {
        log(Level.WARNING, message);
    }

    @Override
    public void logSevere(String message) {
        log(Level.SEVERE, message);
    }
    
    @Override
    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public void pause() {
        if (!isPaused) {
            isPaused = true;
            timerTask.cancel();
        }
    }

    @Override
    public void unpause() {
        if (isPaused) {
            isPaused = false;
            timerTask = createTimerTask();
            timer.schedule(timerTask, 0, periodInMs);
        }
    }

}
