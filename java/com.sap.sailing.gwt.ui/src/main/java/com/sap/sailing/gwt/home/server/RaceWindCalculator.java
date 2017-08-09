package com.sap.sailing.gwt.home.server;

import java.util.Arrays;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.WindFixesFinder;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.confidence.BearingWithConfidenceCluster;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.common.confidence.impl.BearingWithConfidenceImpl;
import com.sap.sailing.domain.tracking.RaceLogWindFixDeclinationHelper;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.gwt.home.communication.race.wind.WindStatisticsDTO;
import com.sap.sse.common.TimePoint;

public class RaceWindCalculator {
    private RaceWindCalculator() {

    }
    
    public static WindStatisticsDTO getWindStatisticsOrNull(TrackedRace trackedRace,TimePoint toTimePoint,TimePoint startTime,RaceLog racelog) {
        final WindStatisticsDTO result;
        if (trackedRace != null) {
            final BearingWithConfidenceCluster<TimePoint> bwcc = new BearingWithConfidenceCluster<TimePoint>(
                    new Weigher<TimePoint>() {
                        private static final long serialVersionUID = -5779398785058438328L;
                        @Override
                        public double getConfidence(TimePoint fix, TimePoint request) {
                            return 1;
                        }
                    });
            if (startTime == null) {
                startTime = toTimePoint;
            }
            final TimePoint middleOfRace = startTime.plus(startTime.until(toTimePoint).divide(2));
            List<TimePoint> pointsToGetWind = Arrays.asList(startTime, middleOfRace, toTimePoint);
            Double lowerBoundWindInKnots = null;
            Double upperBoundWindInKnots = null;
            for (TimePoint timePoint : pointsToGetWind) {
                WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> averagedWindWithConfidence = getWindFromTrackedRace(timePoint,trackedRace);
                if (averagedWindWithConfidence != null) {
                    Wind wind = averagedWindWithConfidence.getObject();
                    bwcc.add(new BearingWithConfidenceImpl<TimePoint>(wind.getBearing(), averagedWindWithConfidence
                            .getConfidence(), timePoint));
                    double currentWindInKnots = wind.getKnots();
                    if (lowerBoundWindInKnots == null) {
                        lowerBoundWindInKnots = currentWindInKnots;
                        upperBoundWindInKnots = currentWindInKnots;
                    } else {
                        lowerBoundWindInKnots = Math.min(lowerBoundWindInKnots, currentWindInKnots);
                        upperBoundWindInKnots = Math.max(upperBoundWindInKnots, currentWindInKnots);
                    }
                }
            }
            if (lowerBoundWindInKnots != null && upperBoundWindInKnots != null) {
                BearingWithConfidence<TimePoint> average = bwcc.getAverage(middleOfRace);
                result = new WindStatisticsDTO(average.getObject().reverse().getDegrees(), lowerBoundWindInKnots,
                        upperBoundWindInKnots);
            } else {
                result = null;
            }
        } else {
            Wind wind = RaceWindCalculator.checkForWindFixesFromRaceLog(racelog);
            if (wind != null) {
                result = new WindStatisticsDTO(wind.getFrom().getDegrees(), wind.getKnots(), wind.getKnots());
            } else {
                result = null;
            }
        }
        return result;
    }

    public static Wind checkForWindFixesFromRaceLog(RaceLog raceLog) {
        final Wind result;
        if (raceLog == null) {
            result = null;
        } else {
            WindFixesFinder windFixesFinder = new WindFixesFinder(raceLog);
            List<RaceLogWindFixEvent> windList = windFixesFinder.analyze();
            if (!windList.isEmpty()) {
                result = new RaceLogWindFixDeclinationHelper()
                        .getOptionallyDeclinationCorrectedWind(windList.get(windList.size() - 1));
            } else {
                result = null;
            }
        }
        return result;
    }

    public static WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> getWindFromTrackedRace(
            TimePoint timePoint, TrackedRace trackedRace) {
        WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> averagedWindWithConfidence = trackedRace
                .getWindWithConfidence(trackedRace.getCenterOfCourse(timePoint), timePoint);
        final WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> result;
        if (averagedWindWithConfidence != null && averagedWindWithConfidence.getObject().getKnots() >= 0.05d) {
            result = averagedWindWithConfidence;
        } else {
            result = null;
        }
        return result;
    }
}
