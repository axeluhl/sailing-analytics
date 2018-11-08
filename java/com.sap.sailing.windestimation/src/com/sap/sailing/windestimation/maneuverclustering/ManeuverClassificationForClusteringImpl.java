package com.sap.sailing.windestimation.maneuverclustering;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.windestimation.ManeuverClassificationImpl;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverClassification;
import com.sap.sailing.windestimation.maneuverclassifier.ProbabilisticManeuverClassifier;

public class ManeuverClassificationForClusteringImpl extends ManeuverClassificationImpl {

    private final ProbabilisticManeuverClassifier maneuverClassifier;
    private final ManeuverForEstimation maneuver;

    public ManeuverClassificationForClusteringImpl(ManeuverForEstimation maneuver, String competitorName,
            PolarDataService polarService, ProbabilisticManeuverClassifier maneuverClassifier) {
        super(competitorName, maneuver.getBoatClass(), maneuver.getManeuverTimePoint(), maneuver.getManeuverPosition(),
                maneuver.getCourseChangeInDegrees(), maneuver.getSpeedWithBearingBefore(), maneuver.getMiddleCourse(),
                null, polarService);
        this.maneuver = maneuver;
        this.maneuverClassifier = maneuverClassifier;
    }

    @Override
    public double getLikelihoodForManeuverType(ManeuverType maneuverType) {
        if (likelihoodPerManeuverType[maneuverType.ordinal()] == null) {
            ManeuverClassification maneuverEstimationResult = maneuverClassifier.classifyManeuver(maneuver);
            likelihoodPerManeuverType[ManeuverType.TACK.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.TACK);
            likelihoodPerManeuverType[ManeuverType.JIBE.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.JIBE);
            likelihoodPerManeuverType[ManeuverType.HEAD_UP.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.HEAD_UP);
            likelihoodPerManeuverType[ManeuverType.BEAR_AWAY.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.BEAR_AWAY);
            likelihoodPerManeuverType[ManeuverType.PENALTY_CIRCLE.ordinal()] = 0.0;
            likelihoodPerManeuverType[ManeuverType.UNKNOWN.ordinal()] = 0.0;
        }
        return likelihoodPerManeuverType[maneuverType.ordinal()];
    }

    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

}
