package com.sap.sailing.windestimation.model.classifier.twdtransition;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.twdtransition.TwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.model.ModelPersistenceException;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class PersistedTwdTransitionClassifiersScorePrinter {

    private PersistedTwdTransitionClassifiersScorePrinter() {
    }

    public static void main(String[] args)
            throws MalformedURLException, ClassNotFoundException, IOException, InterruptedException {
        ModelStore classifierModelStore = new MongoDbModelStore(new TwdTransitionPersistenceManager().getDb());
        List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> allClassifierModels = new ArrayList<>();
        TwdTransitionClassifierModelFactory classifierModelFactory = new TwdTransitionClassifierModelFactory();
        LoggingUtil.logInfo("### Loading classifiers:");
        for (TwdTransitionClassifierModelMetadata modelMetadata : classifierModelFactory
                .getAllValidContextSpecificModelMetadataCandidates(null)) {
            LabelledTwdTransitionClassifierModelMetadata labelledModelMetadata = new LabelledTwdTransitionClassifierModelMetadata(
                    modelMetadata.getManeuverTypeTransition());
            List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata>> classifierModels = classifierModelFactory
                    .getAllTrainableModels(labelledModelMetadata);
            for (TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> classifierModel : classifierModels) {
                try {
                    classifierModel = classifierModelStore.loadPersistedState(classifierModel);
                    if (classifierModel != null) {
                        allClassifierModels.add(classifierModel);
                    }
                } catch (ModelPersistenceException e) {
                }
            }
        }
        StringBuilder str = new StringBuilder("Classifier name \t| Maneuver type transition \t| Test score");
        Collections.sort(allClassifierModels, (a, b) -> Double.compare(a.getTestScore(), b.getTestScore()));
        for (TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> classifierModel : allClassifierModels) {
            TwdTransitionClassifierModelMetadata modelMetadata = classifierModel.getContextSpecificModelMetadata();
            str.append("\r\n" + classifierModel.getClass().getSimpleName() + ": \t| "
                    + modelMetadata.getManeuverTypeTransition() + " \t| "
                    + String.format(" %.03f", classifierModel.getTestScore()));
        }
        String outputStr = str.toString();
        LoggingUtil.logInfo(outputStr);
        outputStr = outputStr.replaceAll(Pattern.quote(" \t| "), ";");
        Files.write(Paths.get("twdTransitionClassifierScores.csv"), outputStr.getBytes());
    }

}
