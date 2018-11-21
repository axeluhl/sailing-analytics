package com.sap.sailing.windestimation.classifier.maneuver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.TrainableManeuverClassificationModel;
import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierLoader {

    private final ClassifierModelStore classifierModelStore;

    public ManeuverClassifierLoader(ClassifierModelStore classifierModelStore) {
        this.classifierModelStore = classifierModelStore;
    }

    public ManeuverClassifier loadBestClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        List<TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> models = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> model = ManeuverClassifierModelFactory
                        .getNewClassifierModel(possibleFeatures, null);
                models.add(model);
                if (boatClass != null) {
                    TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> modelForBoatClass = ManeuverClassifierModelFactory
                            .getNewClassifierModel(possibleFeatures, boatClass);
                    models.add(modelForBoatClass);
                }
            }
        }

        List<TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> loadedModels = new ArrayList<>();
        for (TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> model : models) {
            try {
                boolean loaded = classifierModelStore.loadPersistedState(model);
                if (loaded
                        && !model.getModelMetadata().getContextSpecificModelMetadata().getManeuverFeatures()
                                .isPolarsInformation()
                        || model.getNumberOfTrainingInstances() >= ManeuverClassifiersCache.MIN_FIXES_FOR_POLARS_INFORMATION) {
                    loadedModels.add(model);
                }
            } catch (ClassifierPersistenceException e) {
            }
        }

        if (loadedModels.isEmpty()) {
            return null;
        }
        Iterator<TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> loadedClassifiersIterator = loadedModels
                .iterator();
        TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> bestModel = loadedClassifiersIterator
                .next();
        while (loadedClassifiersIterator.hasNext()) {
            TrainableManeuverClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> otherModel = loadedClassifiersIterator
                    .next();
            if (bestModel.getTestScore() < otherModel.getTestScore()) {
                bestModel = otherModel;
            }
        }
        return new ManeuverClassifierImpl(bestModel);
    }

}
