package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.data.RaceWithWindSources;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.WindSourceWithFixes;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.RaceWithWindSourcesPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.TimePoint;

public class DurationBasedTwdTransitionImporter {

    private static final double SECONDS_INTERVAL_TO_SAMPLE = 1;
    private static final double ANNEALING_FACTOR_FOR_SECONDS_PASSED_FOR_SAMPLING = 0.1;

    public static void main(String[] args) throws UnknownHostException {
        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import started");
        RaceWithWindSourcesPersistenceManager racesPersistenceManager = new RaceWithWindSourcesPersistenceManager();
        SingleDimensionBasedTwdTransitionPersistenceManager durationBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DURATION);
        durationBasedTwdTransitionPersistenceManager.dropCollection();
        long totalValuesCount = 0;
        for (PersistedElementsIterator<RaceWithWindSources> iterator = racesPersistenceManager
                .getIterator(null); iterator.hasNext();) {
            RaceWithWindSources race = iterator.next();
            LoggingUtil.logInfo("Processing race " + race.getRaceName() + " of regatta " + race.getRegattaName());
            List<SingleDimensionBasedTwdTransition> entries = new ArrayList<>();
            TimePoint timePointOfLastConsideredWindFix = null;
            for (WindSourceWithFixes windSource : race.getWindSources()) {
                int windFixIndex = 0;
                for (Wind windFix : windSource.getWindFixes()) {
                    if (timePointOfLastConsideredWindFix == null || timePointOfLastConsideredWindFix
                            .until(windFix.getTimePoint()).asSeconds() >= SECONDS_INTERVAL_TO_SAMPLE) {
                        timePointOfLastConsideredWindFix = windFix.getTimePoint();
                        TimePoint timePointOfLastConsideredOtherWindFix = timePointOfLastConsideredWindFix;
                        for (ListIterator<Wind> otherWindFixesIterator = windSource.getWindFixes()
                                .listIterator(++windFixIndex); otherWindFixesIterator.hasNext();) {
                            Wind otherWindFix = otherWindFixesIterator.next();
                            double secondsPassedSinceLastConsideredWindFix = timePointOfLastConsideredOtherWindFix
                                    .until(otherWindFix.getTimePoint()).asSeconds();
                            double secondsPassed = windFix.getTimePoint().until(otherWindFix.getTimePoint())
                                    .asSeconds();
                            if (secondsPassedSinceLastConsideredWindFix >= SECONDS_INTERVAL_TO_SAMPLE
                                    + secondsPassed * ANNEALING_FACTOR_FOR_SECONDS_PASSED_FOR_SAMPLING) {
                                double absTwdChange = windFix.getBearing().getDifferenceTo(otherWindFix.getBearing())
                                        .abs().getDegrees();
                                SingleDimensionBasedTwdTransition entry = new SingleDimensionBasedTwdTransition(
                                        secondsPassed, absTwdChange);
                                entries.add(entry);
                                timePointOfLastConsideredOtherWindFix = otherWindFix.getTimePoint();
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
                    LoggingUtil.logInfo(
                            totalEntries + " TWD transition entries imported, " + totalValuesCount + " in total");
                }
            }
        }

        LoggingUtil.logInfo("###################\r\nDuration based TWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + totalValuesCount + " TWD transitions imported");
    }

}
