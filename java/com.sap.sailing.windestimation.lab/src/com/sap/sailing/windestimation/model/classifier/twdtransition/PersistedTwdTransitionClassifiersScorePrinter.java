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
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class PersistedTwdTransitionClassifiersScorePrinter {

    private PersistedTwdTransitionClassifiersScorePrinter() {
    }

    public static void main(String[] args)
            throws MalformedURLException, ClassNotFoundException, IOException, InterruptedException {
        ModelStore classifierModelStore = new MongoDbModelStore(new TwdTransitionPersistenceManager().getDb());
        List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext>> allClassifierModels = new ArrayList<>();
        TwdTransitionClassifierModelFactory classifierModelFactory = new TwdTransitionClassifierModelFactory();
        LoggingUtil.logInfo("### Loading classifiers:");
        for (ManeuverTypeTransition maneuverTypeTransition : ManeuverTypeTransition.values()) {
            LabelledTwdTransitionClassifierModelContext modelContext = new LabelledTwdTransitionClassifierModelContext(
                    maneuverTypeTransition);
            List<TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext>> classifierModels = classifierModelFactory
                    .getAllTrainableModels(modelContext);
            for (TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext> classifierModel : classifierModels) {
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
        for (TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext> classifierModel : allClassifierModels) {
            TwdTransitionClassifierModelContext modelContext = classifierModel.getModelContext();
            str.append("\r\n" + classifierModel.getClass().getSimpleName() + ": \t| "
                    + modelContext.getManeuverTypeTransition() + " \t| "
                    + String.format(" %.03f", classifierModel.getTestScore()));
        }
        String outputStr = str.toString();
        LoggingUtil.logInfo(outputStr);
        outputStr = outputStr.replaceAll(Pattern.quote(" \t| "), ";");
        Files.write(Paths.get("twdTransitionClassifierScores.csv"), outputStr.getBytes());
    }

}
