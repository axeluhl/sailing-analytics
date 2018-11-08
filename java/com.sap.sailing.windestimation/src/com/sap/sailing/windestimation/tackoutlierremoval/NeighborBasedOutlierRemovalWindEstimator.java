package com.sap.sailing.windestimation.tackoutlierremoval;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.WindWithConfidenceImpl;
import com.sap.sailing.windestimation.ManeuverClassificationsAggregator;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassification;
import com.sap.sailing.windestimation.maneuverclassifier.ProbabilisticManeuverClassifier;
import com.sap.sailing.windestimation.polarsfitting.PolarsFittingWindEstimation;
import com.sap.sailing.windestimation.util.WindUtil;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class NeighborBasedOutlierRemovalWindEstimator implements ManeuverClassificationsAggregator {

    private static final double MAX_DEVIATON_FROM_AVG_WIND_COURSE = 30;
    private final List<WindWithConfidence<Void>> windTrackWithConfidence = new ArrayList<>();

    public NeighborBasedOutlierRemovalWindEstimator(
            List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks,
            ManeuverClassifiersCache maneuverClassifiersCache, PolarsFittingWindEstimation polarsFitting) {
        for (CompetitorTrackWithEstimationData<ManeuverForEstimation> competitorTrack : competitorTracks) {
            for (ManeuverForEstimation maneuver : competitorTrack.getElements()) {
                if (maneuver.isClean() && (maneuver.getDeviationFromOptimalTackAngleInDegrees() == null
                        || Math.abs(maneuver.getDeviationFromOptimalTackAngleInDegrees()) < 6)) {
                    ProbabilisticManeuverClassifier classifier = maneuverClassifiersCache.getBestClassifier(maneuver);
                    ManeuverClassification estimationResult = classifier.classifyManeuver(maneuver);
                    double highestLikelihood = 0;
                    ManeuverTypeForClassification maneuverTypeWithHighestLikelihood = null;
                    ManeuverTypeForClassification[] maneuverTypes = { ManeuverTypeForClassification.TACK,
                            ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.HEAD_UP,
                            ManeuverTypeForClassification.BEAR_AWAY };
                    for (ManeuverTypeForClassification maneuverType : maneuverTypes) {
                        double likelihood = estimationResult.getManeuverTypeLikelihood(maneuverType);
                        if (highestLikelihood < likelihood) {
                            highestLikelihood = likelihood;
                            maneuverTypeWithHighestLikelihood = maneuverType;
                        }
                    }
                    if (maneuverTypeWithHighestLikelihood == ManeuverTypeForClassification.TACK) {
                        Bearing windCourse = maneuver.getMiddleCourse().reverse();
                        Speed windSpeed = polarsFitting == null ? new KnotSpeedImpl(0)
                                : polarsFitting.getWindSpeed(maneuver, windCourse);
                        Wind wind = new WindImpl(maneuver.getManeuverPosition(), maneuver.getManeuverTimePoint(),
                                new KnotSpeedWithBearingImpl(windSpeed.getKnots(), windCourse));
                        windTrackWithConfidence.add(new WindWithConfidenceImpl<Void>(wind, highestLikelihood, null,
                                windSpeed.getKnots() >= 2));
                    }
                }
            }
        }
    }

    @Override
    public List<WindWithConfidence<Void>> estimateWindTrack() {
        Wind previous = null;
        Wind current = null;
        Wind firstNonOutlier = null;
        Wind firstNearlyNonOutlier = null;
        for (WindWithConfidence<Void> windWithConfidence : windTrackWithConfidence) {
            Wind next = windWithConfidence.getObject();
            if (current != null && Math.abs(current.getBearing().getDifferenceTo(next.getBearing())
                    .getDegrees()) <= MAX_DEVIATON_FROM_AVG_WIND_COURSE) {
                if (firstNearlyNonOutlier == null) {
                    firstNearlyNonOutlier = current;
                }
                if (previous != null && Math.abs(current.getBearing().getDifferenceTo(previous.getBearing())
                        .getDegrees()) <= MAX_DEVIATON_FROM_AVG_WIND_COURSE) {
                    firstNonOutlier = current;
                    break;
                }
            }
            previous = current;
            current = next;
        }

        Wind lastNonOutlier = firstNonOutlier == null ? firstNearlyNonOutlier : firstNonOutlier;
        if (lastNonOutlier == null) {
            return windTrackWithConfidence;
        }
        List<WindWithConfidence<Void>> windFixes = new ArrayList<>();
        double sumOfConfidencesOfIncludedFixes = 0;
        double sumOfConfidencesOfExcludedFixes = 0;
        for (WindWithConfidence<Void> windWithConfidence : windTrackWithConfidence) {
            current = windWithConfidence.getObject();
            if (Math.abs(current.getBearing().getDifferenceTo(lastNonOutlier.getBearing())
                    .getDegrees()) <= MAX_DEVIATON_FROM_AVG_WIND_COURSE) {
                windFixes.add(windWithConfidence);
                lastNonOutlier = current;
                sumOfConfidencesOfIncludedFixes += windWithConfidence.getConfidence();
            } else {
                sumOfConfidencesOfExcludedFixes += windWithConfidence.getConfidence();
            }
        }
        double finalConfidence = sumOfConfidencesOfIncludedFixes
                / (sumOfConfidencesOfIncludedFixes + sumOfConfidencesOfExcludedFixes);
        windFixes = WindUtil.getWindFixesWithFixedConfidence(windFixes, finalConfidence);
//        windFixes = WindUtil.getWindFixesWithAveragedWindSpeed(windFixes);
        return windFixes;
    }

}
