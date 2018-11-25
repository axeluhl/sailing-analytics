package com.sap.sailing.windestimation.classifier.maneuver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierLoader {

    private final ClassifierModelStore classifierModelStore;

    public ManeuverClassifierLoader(ClassifierModelStore classifierModelStore) {
        this.classifierModelStore = classifierModelStore;
    }

    public ManeuverClassifier loadBestClassifier(ManeuverFeatures maneuverFeatures, BoatClass boatClass) {
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> models = new ArrayList<>();
        for (ManeuverFeatures possibleFeatures : ManeuverFeatures.values()) {
            if (possibleFeatures.isSubset(maneuverFeatures)) {
                TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> model = ManeuverClassifierModelFactory
                        .getNewClassifierModel(possibleFeatures, null);
                models.add(model);
                if (boatClass != null) {
                    TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> modelForBoatClass = ManeuverClassifierModelFactory
                            .getNewClassifierModel(possibleFeatures, boatClass);
                    models.add(modelForBoatClass);
                }
            }
        }

        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> loadedModels = new ArrayList<>();
        for (TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> model : models) {
            try {
                model = classifierModelStore.loadPersistedState(model);
                if (model != null
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

        Iterator<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> loadedClassifiersIterator = loadedModels
                .iterator();
        TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> bestModel = loadedClassifiersIterator
                .next();
        while (loadedClassifiersIterator.hasNext()) {
            TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> otherModel = loadedClassifiersIterator
                    .next();
            if (bestModel.getTestScore() < otherModel.getTestScore()) {
                bestModel = otherModel;
            }
        }
        return new ManeuverClassifierImpl(bestModel);
    }

}
