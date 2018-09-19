package com.sap.sailing.windestimation.tackoutlierremoval;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.WindTrackEstimator;
import com.sap.sailing.windestimation.data.CoarseGrainedManeuverType;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverEstimationResult;
import com.sap.sailing.windestimation.maneuverclassifier.ProbabilisticManeuverClassifier;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.util.WindUtil;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class TackOutlierRemovalWindEstimator implements WindTrackEstimator {

    private static final double MAX_DEVIATON_FROM_AVG_WIND_COURSE = 30;
    private final List<WindWithConfidence<Void>> windTrackWithConfidence = new ArrayList<>();
    private ScalableBearing windCourseSum = null;
    private double likelihoodSum = 0;

    public TackOutlierRemovalWindEstimator(
            List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks,
            ManeuverClassifiersCache maneuverClassifiersCache, PolarsFittingWindEstimation polarsFitting) {
        for (CompetitorTrackWithEstimationData<ManeuverForEstimation> competitorTrack : competitorTracks) {
            for (ManeuverForEstimation maneuver : competitorTrack.getElements()) {
                if (maneuver.isClean()) {
                    ProbabilisticManeuverClassifier classifier = maneuverClassifiersCache.getBestClassifier(maneuver);
                    ManeuverEstimationResult estimationResult = classifier.classifyManeuver(maneuver);
                    double highestLikelihood = 0;
                    CoarseGrainedManeuverType maneuverTypeWithHighestLikelihood = null;
                    CoarseGrainedManeuverType[] maneuverTypes = { CoarseGrainedManeuverType.TACK,
                            CoarseGrainedManeuverType.JIBE, CoarseGrainedManeuverType.HEAD_UP,
                            CoarseGrainedManeuverType.BEAR_AWAY };
                    for (CoarseGrainedManeuverType maneuverType : maneuverTypes) {
                        double likelihood = estimationResult.getManeuverTypeLikelihood(maneuverType);
                        if (highestLikelihood < likelihood) {
                            highestLikelihood = likelihood;
                            maneuverTypeWithHighestLikelihood = maneuverType;
                        }
                    }
                    if (maneuverTypeWithHighestLikelihood == CoarseGrainedManeuverType.TACK) {
                        Bearing windCourse = maneuver.getMiddleCourse().reverse();
                        Speed windSpeed = polarsFitting == null ? new KnotSpeedImpl(0)
                                : polarsFitting.getWindSpeed(maneuver, windCourse);
                        Wind wind = new WindImpl(maneuver.getManeuverPosition(), maneuver.getManeuverTimePoint(),
                                new KnotSpeedWithBearingImpl(windSpeed.getKnots(), windCourse));
                        windTrackWithConfidence.add(new WindWithConfidenceImpl<Void>(wind, highestLikelihood, null,
                                windSpeed.getKnots() >= 2));
                        ScalableBearing scalableWindCourse = new ScalableBearing(wind.getBearing())
                                .multiply(highestLikelihood);
                        likelihoodSum += highestLikelihood;
                        windCourseSum = windCourseSum == null ? scalableWindCourse
                                : windCourseSum.add(scalableWindCourse);
                    }
                }
            }
        }
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrack() {
        List<WindWithConfidence<Void>> windFixes = new ArrayList<>();
        if (windCourseSum != null) {
            Bearing averageWindCourse = windCourseSum.divide(likelihoodSum);
            for (WindWithConfidence<Void> windWithConfidence : windTrackWithConfidence) {
                Wind wind = windWithConfidence.getObject();
                if (Math.abs(averageWindCourse.getDifferenceTo(wind.getBearing())
                        .getDegrees()) <= MAX_DEVIATON_FROM_AVG_WIND_COURSE) {
                    windFixes.add(windWithConfidence);
                }
            }
        }
        windFixes = WindUtil.getWindFixesWithAveragedWindSpeed(windFixes);
        return windFixes;
    }

}
