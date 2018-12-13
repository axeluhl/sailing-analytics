package com.sap.sailing.windestimation.classifier.twdtransition;

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
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.persistence.twdtransition.TwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class PersistedTwdTransitionClassifiersScorePrinter {

    private PersistedTwdTransitionClassifiersScorePrinter() {

    }

    public static void main(String[] args)
            throws MalformedURLException, ClassNotFoundException, IOException, InterruptedException {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        ModelStore classifierModelStore = new MongoDbModelStore(
                new TwdTransitionPersistenceManager().getDb());
        List<TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata>> allClassifierModels = new ArrayList<>();
        TwdTransitionClassifierModelFactory classifierModelFactory = new TwdTransitionClassifierModelFactory();
        LoggingUtil.logInfo("### Loading all boat class classifiers:");
        LabelledTwdTransitionModelMetadata maneuverModelMetadata = new LabelledTwdTransitionModelMetadata(null);
        List<TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata>> classifierModels = classifierModelFactory
                .getAllTrainableClassifierModels(maneuverModelMetadata);
        for (TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> classifierModel : classifierModels) {
            try {
                classifierModel = classifierModelStore.loadPersistedState(classifierModel);
                if (classifierModel != null) {
                    allClassifierModels.add(classifierModel);
                }
            } catch (ClassifierPersistenceException e) {
            }
        }
        for (BoatClass boatClass : allBoatClasses) {
            maneuverModelMetadata = new LabelledTwdTransitionModelMetadata(boatClass);
            classifierModels = classifierModelFactory.getAllTrainableClassifierModels(maneuverModelMetadata);
            for (TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> classifierModel : classifierModels) {
                try {
                    classifierModel = classifierModelStore.loadPersistedState(classifierModel);
                    if (classifierModel != null) {
                        allClassifierModels.add(classifierModel);
                    }
                } catch (ClassifierPersistenceException e) {
                }
            }
        }
        StringBuilder str = new StringBuilder("Classifier name \t| Boat class \t| Test score");
        Collections.sort(allClassifierModels, (a, b) -> Double.compare(a.getTestScore(), b.getTestScore()));
        for (TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> classifierModel : allClassifierModels) {
            TwdTransitionModelMetadata modelMetadata = classifierModel.getModelMetadata()
                    .getContextSpecificModelMetadata();
            str.append("\r\n" + classifierModel.getClass().getSimpleName() + ": \t| " + modelMetadata.getBoatClass()
                    + " \t| " + String.format(" %.03f", classifierModel.getTestScore()));
        }
        String outputStr = str.toString();
        LoggingUtil.logInfo(outputStr);
        outputStr = outputStr.replaceAll(Pattern.quote(" \t| "), ";");
        Files.write(Paths.get("twdTransitionClassifierScores.csv"), outputStr.getBytes());
    }

}
