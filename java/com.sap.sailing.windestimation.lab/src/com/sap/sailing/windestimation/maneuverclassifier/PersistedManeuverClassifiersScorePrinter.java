package com.sap.sailing.windestimation.maneuverclassifier;

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
import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverClassifierModelFactory;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverModelMetadata;
import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;
import com.sap.sailing.windestimation.classifier.store.MongoDbClassifierModelStore;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.persistence.maneuver.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class PersistedManeuverClassifiersScorePrinter {

    private PersistedManeuverClassifiersScorePrinter() {

    }

    public static void main(String[] args)
            throws MalformedURLException, ClassNotFoundException, IOException, InterruptedException {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        RegularManeuversForEstimationPersistenceManager persistenceManager = new RegularManeuversForEstimationPersistenceManager();
        ClassifierModelStore classifierModelStore = new MongoDbClassifierModelStore(persistenceManager.getDb());
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> allClassifierModels = new ArrayList<>();
        LoggingUtil.logInfo("### Loading all boat class classifiers:");
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> classifierModels = ManeuverClassifierModelFactory
                    .getAllTrainableClassifierModels(maneuverFeatures, null);
            for (TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classifierModel : classifierModels) {
                try {
                    if (classifierModelStore.loadPersistedState(classifierModel)) {
                        allClassifierModels.add(classifierModel);
                    }
                } catch (ClassifierPersistenceException e) {
                }
            }
            for (BoatClass boatClass : allBoatClasses) {
                classifierModels = ManeuverClassifierModelFactory.getAllTrainableClassifierModels(maneuverFeatures,
                        boatClass);
                for (TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classifierModel : classifierModels) {
                    try {
                        if (classifierModelStore.loadPersistedState(classifierModel)) {
                            allClassifierModels.add(classifierModel);
                        }
                    } catch (ClassifierPersistenceException e) {
                    }
                }
            }
        }
        StringBuilder str = new StringBuilder("Classifier name \t| Maneuver features \t| Boat class \t| Test score");
        Collections.sort(allClassifierModels, (a, b) -> Double.compare(a.getTestScore(), b.getTestScore()));
        for (TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classifierModel : allClassifierModels) {
            ManeuverModelMetadata modelMetadata = classifierModel.getModelMetadata().getContextSpecificModelMetadata();
            str.append("\r\n" + classifierModel.getClass().getSimpleName() + ": \t| "
                    + modelMetadata.getManeuverFeatures() + " \t| " + modelMetadata.getBoatClass() + " \t| "
                    + String.format(" %.03f", classifierModel.getTestScore()));
        }
        String outputStr = str.toString();
        LoggingUtil.logInfo(outputStr);
        outputStr = outputStr.replaceAll(Pattern.quote(" \t| "), ";");
        Files.write(Paths.get("classifierScores.csv"), outputStr.getBytes());
    }

}
