package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.windestimation.classifier.ClassificationModel;
import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public class ManeuverClassifierImpl implements ManeuverClassifier {

    private final ClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> model;
    private final ManeuverModelMetadata modelMetadata;

    public ManeuverClassifierImpl(ClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> model) {
        this.model = model;
        this.modelMetadata = model.getModelMetadata().getContextSpecificModelMetadata();
    }

    @Override
    public ManeuverWithProbabilisticTypeClassification classifyManeuver(ManeuverForEstimation maneuver) {
        double[] x = modelMetadata.getX(maneuver);
        double[] likelihoods = model.classifyWithProbabilities(x);
        likelihoods = modelMetadata.getLikelihoodsPerManeuverTypeOrdinal(likelihoods);
        likelihoods = mapManeuverTypesForInternalClassificationToManeuverTypesForClassification(likelihoods,
                maneuver.getManeuverCategory());
        ManeuverWithProbabilisticTypeClassification maneuverClassificationResult = new ManeuverWithProbabilisticTypeClassification(
                maneuver, likelihoods);
        return maneuverClassificationResult;
    }

    private double[] mapManeuverTypesForInternalClassificationToManeuverTypesForClassification(
            double[] likelihoodPerManeuverType, ManeuverCategory maneuverCategory) {
        double[] newLikelihoods = new double[ManeuverTypeForClassification.values().length];
        switch (maneuverCategory) {
        case MARK_PASSING:
        case REGULAR:
            newLikelihoods[ManeuverTypeForClassification.TACK
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.TACK.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.JIBE
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.JIBE.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.BEAR_AWAY
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.OTHER.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.HEAD_UP
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.OTHER.ordinal()];
            break;
        default:
            break;
        }
        return newLikelihoods;
    }

    @Override
    public double getTestScore() {
        return model.getTestScore();
    }

    @Override
    public boolean hasSupportForProvidedFeatures() {
        return model.hasSupportForProvidedFeatures();
    }

}
