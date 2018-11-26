package com.sap.sailing.windestimation.aggregator.clustering;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.windestimation.ManeuverClassificationImpl;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public class ManeuverClassificationForClusteringImpl extends ManeuverClassificationImpl {

    private final ManeuverWithProbabilisticTypeClassification maneuverWithProbabilisticTypeClassification;

    public ManeuverClassificationForClusteringImpl(
            ManeuverWithProbabilisticTypeClassification maneuverWithProbabilisticTypeClassification,
            String competitorName, PolarDataService polarService) {
        super(competitorName, maneuverWithProbabilisticTypeClassification.getManeuver().getBoatClass(),
                maneuverWithProbabilisticTypeClassification.getManeuver().getManeuverTimePoint(),
                maneuverWithProbabilisticTypeClassification.getManeuver().getManeuverPosition(),
                maneuverWithProbabilisticTypeClassification.getManeuver().getCourseChangeInDegrees(),
                maneuverWithProbabilisticTypeClassification.getManeuver().getSpeedWithBearingBefore(),
                maneuverWithProbabilisticTypeClassification.getManeuver().getMiddleCourse(), null, polarService);
        this.maneuverWithProbabilisticTypeClassification = maneuverWithProbabilisticTypeClassification;
    }

    @Override
    public double getLikelihoodForManeuverType(ManeuverType maneuverType) {
        if (likelihoodPerManeuverType[maneuverType.ordinal()] == null) {
            likelihoodPerManeuverType[ManeuverType.TACK.ordinal()] = maneuverWithProbabilisticTypeClassification
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.TACK);
            likelihoodPerManeuverType[ManeuverType.JIBE.ordinal()] = maneuverWithProbabilisticTypeClassification
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.JIBE);
            likelihoodPerManeuverType[ManeuverType.HEAD_UP.ordinal()] = maneuverWithProbabilisticTypeClassification
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.HEAD_UP);
            likelihoodPerManeuverType[ManeuverType.BEAR_AWAY.ordinal()] = maneuverWithProbabilisticTypeClassification
                    .getManeuverTypeLikelihood(ManeuverTypeForClassification.BEAR_AWAY);
            likelihoodPerManeuverType[ManeuverType.PENALTY_CIRCLE.ordinal()] = 0.0;
            likelihoodPerManeuverType[ManeuverType.UNKNOWN.ordinal()] = 0.0;
        }
        return likelihoodPerManeuverType[maneuverType.ordinal()];
    }

    public ManeuverForEstimation getManeuver() {
        return maneuverWithProbabilisticTypeClassification.getManeuver();
    }

}
