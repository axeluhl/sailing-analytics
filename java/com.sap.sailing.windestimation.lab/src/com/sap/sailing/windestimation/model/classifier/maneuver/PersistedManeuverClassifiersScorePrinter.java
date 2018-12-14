package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.persistence.maneuver.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.model.ModelPersistenceException;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierModelFactory;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierModelMetadata;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class PersistedManeuverClassifiersScorePrinter {

    private PersistedManeuverClassifiersScorePrinter() {

    }

    public static void main(String[] args)
            throws MalformedURLException, ClassNotFoundException, IOException, InterruptedException {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        ModelStore classifierModelStore = new MongoDbModelStore(
                new RegularManeuversForEstimationPersistenceManager().getDb());
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata>> allClassifierModels = new ArrayList<>();
        ManeuverClassifierModelFactory classifierModelFactory = new ManeuverClassifierModelFactory();
        LoggingUtil.logInfo("### Loading all boat class classifiers:");
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            LabelledManeuverModelMetadata maneuverModelMetadata = new LabelledManeuverModelMetadata(maneuverFeatures,
                    null, ManeuverClassifierModelFactory.orderedSupportedTargetValues);
            List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata>> classifierModels = classifierModelFactory
                    .getAllTrainableModels(maneuverModelMetadata);
            for (TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> classifierModel : classifierModels) {
                try {
                    classifierModel = classifierModelStore.loadPersistedState(classifierModel);
                    if (classifierModel != null) {
                        allClassifierModels.add(classifierModel);
                    }
                } catch (ModelPersistenceException e) {
                }
            }
            for (BoatClass boatClass : allBoatClasses) {
                maneuverModelMetadata = new LabelledManeuverModelMetadata(maneuverFeatures, boatClass,
                        ManeuverClassifierModelFactory.orderedSupportedTargetValues);
                classifierModels = classifierModelFactory.getAllTrainableModels(maneuverModelMetadata);
                for (TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> classifierModel : classifierModels) {
                    try {
                        classifierModel = classifierModelStore.loadPersistedState(classifierModel);
                        if (classifierModel != null) {
                            allClassifierModels.add(classifierModel);
                        }
                    } catch (ModelPersistenceException e) {
                    }
                }
            }
        }
        StringBuilder str = new StringBuilder("Classifier name \t| Maneuver features \t| Boat class \t| Test score");
        Collections.sort(allClassifierModels, (a, b) -> Double.compare(a.getTestScore(), b.getTestScore()));
        for (TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> classifierModel : allClassifierModels) {
            ManeuverClassifierModelMetadata modelMetadata = classifierModel.getContextSpecificModelMetadata();
            str.append("\r\n" + classifierModel.getClass().getSimpleName() + ": \t| "
                    + modelMetadata.getManeuverFeatures() + " \t| " + modelMetadata.getBoatClass() + " \t| "
                    + String.format(" %.03f", classifierModel.getTestScore()));
        }
        String outputStr = str.toString();
        LoggingUtil.logInfo(outputStr);
        outputStr = outputStr.replaceAll(Pattern.quote(" \t| "), ";");
        Files.write(Paths.get("maneuverClassifierScores.csv"), outputStr.getBytes());
    }

}
