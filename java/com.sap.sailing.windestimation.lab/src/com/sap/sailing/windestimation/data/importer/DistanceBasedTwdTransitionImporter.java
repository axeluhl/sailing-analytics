package com.sap.sailing.windestimation.data.importer;

import java.net.UnknownHostException;
import java.util.Iterator;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.windestimation.data.SingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.SingleDimensionBasedTwdTransitionPersistenceManager.SingleDimensionType;
import com.sap.sailing.windestimation.data.persistence.twdtransition.WindByTimePersistenceManager;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeWindow;
import com.sap.sse.common.Util.Pair;

public class DistanceBasedTwdTransitionImporter {

    public static final Duration TOLERANCE = Duration.ofSeconds(5);
    public static final int SAMPLING_SECONDS = 1;
    public static final int MAX_DISTANCE_METERS = 1000000;
    public static final int MIN_DISTANCE_METERS = 10;

    public static void main(String[] args) throws UnknownHostException {
        new DistanceBasedTwdTransitionImporter().importDistanceBasedTwdTransition();
    }
    
    private static class WindByPositionEquivalence {
        private final Wind wind;

        public WindByPositionEquivalence(Wind wind) {
            super();
            this.wind = wind;
        }
        
        @Override
        public int hashCode() {
            return 257 ^ wind.getPosition().hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            return wind.getPosition().equals(((WindByPositionEquivalence) o).wind.getPosition());
        }
    }
    
    public void importDistanceBasedTwdTransition() throws UnknownHostException {
        LoggingUtil.logInfo("###################\r\nDistance based TWD transitions Import started");
        final WindByTimePersistenceManager windByTimeManager = new WindByTimePersistenceManager();
        SingleDimensionBasedTwdTransitionPersistenceManager distanceBasedTwdTransitionPersistenceManager = new SingleDimensionBasedTwdTransitionPersistenceManager(
                SingleDimensionType.DISTANCE);
        distanceBasedTwdTransitionPersistenceManager.dropCollection();
        long totalValuesCount = 0;
        Wind previousFirstElementOfPair = null;
        long numberOfWindFixes = windByTimeManager.countElements();
        long windFixNumber = 0;
        final Iterator<Wind> iterator = windByTimeManager.getWindNewerThan(TimePoint.BeginningOfTime).iterator();
        long oldPercent = 0;
        final TimeWindow<Wind> timeWindow = new TimeWindow<>(iterator, TOLERANCE, WindByPositionEquivalence::new);
        while (timeWindow.hasNext()) {
            final Pair<Wind, Wind> nextFixPair = timeWindow.next();
            // count progress each time the pair's first element changes:
            if (nextFixPair.getA() != previousFirstElementOfPair) {
                windFixNumber++;
                final long percent = windFixNumber++ * 100 / numberOfWindFixes;
                if (percent > oldPercent) {
                    LoggingUtil.logInfo("Processing wind fix " + windFixNumber + "/" + numberOfWindFixes + " ("
                            + percent + "%) for distance dimension");
                    oldPercent = percent;
                }
                previousFirstElementOfPair = nextFixPair.getA();
            }
            final double meters = nextFixPair.getA().getPosition().getDistance(nextFixPair.getB().getPosition()).getMeters();
            if (meters <= MAX_DISTANCE_METERS && meters >= MIN_DISTANCE_METERS) {
                double twdChangeInDegrees = nextFixPair.getA().getBearing().getDifferenceTo(nextFixPair.getB().getBearing()).getDegrees();
                SingleDimensionBasedTwdTransition entry = new SingleDimensionBasedTwdTransition(meters, twdChangeInDegrees);
                distanceBasedTwdTransitionPersistenceManager.add(entry);
                totalValuesCount++;
            }
        }
        LoggingUtil.logInfo(totalValuesCount + " distance based TWD transitions imported in total");
        LoggingUtil.logInfo("###################\r\nDistance based TWD transitions Import finished");
        LoggingUtil.logInfo("Totally " + totalValuesCount + " distance based TWD transitions imported");
    }

}
