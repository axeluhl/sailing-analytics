package com.sap.sailing.server.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.util.EventUtil;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

/**
 * Calculates {@link Statistics} per year for all locally available events.
 */
public class ScheduledStatisticsUpdater {
    private static final int UPDATE_INTERVAL_IN_SECONDS = 60;
    private NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock("lock for " + ScheduledStatisticsUpdater.class.getSimpleName(), true);

    private static final Logger logger = Logger.getLogger(ScheduledStatisticsUpdater.class.getName());
    
    private final Map<Integer, Statistics> statisticsPerYear;
    private final RacingEventService racingEventService;

    /**
     * @param scheduler
     *            Used to schedule the periodic statistics calculations
     */
    public ScheduledStatisticsUpdater(ScheduledExecutorService scheduler, RacingEventService racingEventService) {
        this.racingEventService = racingEventService;
        statisticsPerYear = new ConcurrentHashMap<>();
        scheduler.scheduleWithFixedDelay(this::update, /* initialDelay */ 0, UPDATE_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void clear() {
        LockUtil.executeWithWriteLock(lock, () -> {
            statisticsPerYear.clear();
        });
    }

    private void update() {
        logger.fine("Starting update of local statistics");
        final Map<Integer, Statistics> statisticsByYear = calculateStatisticsByYear();
        LockUtil.executeWithReadLock(lock, () -> {
            this.statisticsPerYear.clear();
            this.statisticsPerYear.putAll(statisticsByYear);
        });
        logger.finest("Finished update of local statistics");
    }
    
    private Map<Integer, Statistics> calculateStatisticsByYear() {
        final Map<Integer, StatisticsCalculator> calculators = new HashMap<>();
        racingEventService.getAllEvents().forEach((event) -> {
            final Integer eventYear = EventUtil.getYearOfEvent(event);
            final StatisticsCalculator calculator;
            if (calculators.containsKey(eventYear)) {
                calculator = calculators.get(eventYear);
            } else {
                calculator = new StatisticsCalculator();
                calculators.put(eventYear, calculator);
            }
            event.getLeaderboardGroups().forEach((lg) -> {
                lg.getLeaderboards().forEach(calculator::addLeaderboard);
            });
        });
        Map<Integer, Statistics> result = new HashMap<>();
        calculators.forEach((year, calculator) -> {
            result.put(year, calculator.getStatistics());
        });
        return result;
    }
    
    public Map<Integer, Statistics> getStatisticsPerYear() {
        return Collections.unmodifiableMap(statisticsPerYear);
    }
}
