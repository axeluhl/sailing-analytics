package com.sap.sailing.windestimation.maneuverclustering;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.polars.windestimation.ManeuverClassificationImpl;
import com.sap.sailing.windestimation.data.CoarseGrainedManeuverType;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverEstimationResult;
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
            ManeuverEstimationResult maneuverEstimationResult = maneuverClassifier.classifyManeuver(maneuver);
            likelihoodPerManeuverType[ManeuverType.TACK.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(CoarseGrainedManeuverType.TACK);
            likelihoodPerManeuverType[ManeuverType.JIBE.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(CoarseGrainedManeuverType.JIBE);
            likelihoodPerManeuverType[ManeuverType.HEAD_UP.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(CoarseGrainedManeuverType.HEAD_UP);
            likelihoodPerManeuverType[ManeuverType.BEAR_AWAY.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(CoarseGrainedManeuverType.BEAR_AWAY);
            likelihoodPerManeuverType[ManeuverType.PENALTY_CIRCLE.ordinal()] = maneuverEstimationResult
                    .getManeuverTypeLikelihood(CoarseGrainedManeuverType._360);
            likelihoodPerManeuverType[ManeuverType.UNKNOWN.ordinal()] = 0.0;
        }
        return likelihoodPerManeuverType[maneuverType.ordinal()];
    }
    
    public ManeuverForEstimation getManeuver() {
        return maneuver;
    }

}
