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
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.FileSystemModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class PersistedManeuverClassifiersScorePrinter {

    private PersistedManeuverClassifiersScorePrinter() {
    }

    public static void main(String[] args)
            throws MalformedURLException, ClassNotFoundException, IOException, InterruptedException {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        ModelStore classifierModelStore = new FileSystemModelStoreImpl("trained_wind_estimation_models");
        // ModelStore classifierModelStore = new MongoDbModelStore(
        // new RegularManeuversForEstimationPersistenceManager().getDb());
        List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext>> allClassifierModels = new ArrayList<>();
        ManeuverClassifierModelFactory classifierModelFactory = new ManeuverClassifierModelFactory();
        LoggingUtil.logInfo("### Loading all boat class classifiers:");
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            LabelledManeuverClassifierModelContext modelContext = new LabelledManeuverClassifierModelContext(
                    maneuverFeatures, null, ManeuverClassifierModelFactory.orderedSupportedTargetValues);
            List<TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext>> classifierModels = classifierModelFactory
                    .getAllTrainableModels(modelContext);
            for (TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel : classifierModels) {
                try {
                    classifierModel = classifierModelStore.loadModel(classifierModel);
                    if (classifierModel != null) {
                        allClassifierModels.add(classifierModel);
                    }
                } catch (ModelPersistenceException e) {
                }
            }
            for (BoatClass boatClass : allBoatClasses) {
                modelContext = new LabelledManeuverClassifierModelContext(maneuverFeatures,
                        boatClass.getName(), ManeuverClassifierModelFactory.orderedSupportedTargetValues);
                classifierModels = classifierModelFactory.getAllTrainableModels(modelContext);
                for (TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel : classifierModels) {
                    try {
                        classifierModel = classifierModelStore.loadModel(classifierModel);
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
        for (TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel : allClassifierModels) {
            ManeuverClassifierModelContext modelContext = classifierModel.getModelContext();
            str.append("\r\n" + classifierModel.getClass().getSimpleName() + ": \t| "
                    + modelContext.getManeuverFeatures() + " \t| " + modelContext.getBoatClassName() + " \t| "
                    + String.format(" %.03f", classifierModel.getTestScore()));
        }
        String outputStr = str.toString();
        LoggingUtil.logInfo(outputStr);
        outputStr = outputStr.replaceAll(Pattern.quote(" \t| "), ";");
        Files.write(Paths.get("maneuverClassifierScores.csv"), outputStr.getBytes());
    }

}
