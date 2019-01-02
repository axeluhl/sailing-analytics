package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;
import com.sap.sailing.windestimation.data.persistence.twdtransition.WindSourcesPersistenceManager;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.TimePoint;

public class DurationBasedTwdTransitionImporter {

    public static final double SECONDS_INTERVAL_TO_SAMPLE = 1;
    public static final double ANNEALING_FACTOR_FOR_SECONDS_PASSED_FOR_SAMPLING = 1.5;

    public static void main(String[] args) throws UnknownHostException {
        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import started");
        WindSourcesPersistenceManager windSourcesPersistenceManager = new WindSourcesPersistenceManager();
        SingleDimensionBasedTwdTransitionPersistenceManager durationBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DURATION);
        durationBasedTwdTransitionPersistenceManager.dropCollection();
        long totalValuesCount = 0;
        long numberOfWindSources = windSourcesPersistenceManager.countElements();
        long windSourceNumber = 1;
        for (PersistedElementsIterator<WindSourceWithFixes> iterator = windSourcesPersistenceManager
                .getIterator(); iterator.hasNext();) {
            long percent = windSourceNumber * 100 / numberOfWindSources;
            WindSourceWithFixes windSource = iterator.next();
            LoggingUtil.logInfo("Processing " + windSourceNumber++ + "/" + numberOfWindSources + " (" + percent + "%)");
            TimePoint timePointOfLastConsideredWindFix = null;
            List<SingleDimensionBasedTwdTransition> entries = new ArrayList<>();
            int windFixIndex = 0;
            for (Wind windFix : windSource.getWindFixes()) {
                if (timePointOfLastConsideredWindFix == null || timePointOfLastConsideredWindFix
                        .until(windFix.getTimePoint()).asSeconds() >= SECONDS_INTERVAL_TO_SAMPLE) {
                    timePointOfLastConsideredWindFix = windFix.getTimePoint();
                    TimePoint timePointOfLastConsideredOtherWindFix = timePointOfLastConsideredWindFix;
                    double lastSecondsPassed = 0;
                    for (ListIterator<Wind> otherWindFixesIterator = windSource.getWindFixes()
                            .listIterator(++windFixIndex); otherWindFixesIterator.hasNext();) {
                        Wind otherWindFix = otherWindFixesIterator.next();
                        double secondsPassedSinceLastConsideredWindFix = timePointOfLastConsideredOtherWindFix
                                .until(otherWindFix.getTimePoint()).asSeconds();
                        double secondsPassed = windFix.getTimePoint().until(otherWindFix.getTimePoint()).asSeconds();
                        if (secondsPassedSinceLastConsideredWindFix >= SECONDS_INTERVAL_TO_SAMPLE
                                + lastSecondsPassed * ANNEALING_FACTOR_FOR_SECONDS_PASSED_FOR_SAMPLING) {
                            double absTwdChange = windFix.getBearing().getDifferenceTo(otherWindFix.getBearing())
                                    .getDegrees();
                            SingleDimensionBasedTwdTransition entry = new SingleDimensionBasedTwdTransition(
                                    secondsPassed, absTwdChange);
                            entries.add(entry);
                            timePointOfLastConsideredOtherWindFix = otherWindFix.getTimePoint();
                            lastSecondsPassed = secondsPassed;
                        }
                    }
                }
            }
            if (entries.isEmpty()) {
                LoggingUtil.logInfo("No TWD transitions to import");
            } else {
                durationBasedTwdTransitionPersistenceManager.add(entries);
                int totalEntries = entries.size();
                totalValuesCount += totalEntries;
                LoggingUtil
                        .logInfo(totalEntries + " TWD transition entries imported, " + totalValuesCount + " in total");
            }
        }

        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + totalValuesCount + " TWD transitions imported");
    }

}
