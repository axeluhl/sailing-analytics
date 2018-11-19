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
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class PersistedManeuverClassifiersScorePrinter {

    private PersistedManeuverClassifiersScorePrinter() {

    }

    public static void main(String[] args)
            throws MalformedURLException, ClassNotFoundException, IOException, InterruptedException {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        List<TrainableSingleManeuverOfflineClassifier> allBoatClassesClassifiers = new ArrayList<>();
        LoggingUtil.logInfo("### Loading all boat class classifiers:");
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            List<TrainableSingleManeuverOfflineClassifier> classifiers = ManeuverClassifiersFactory
                    .getAllTrainableClassifierInstances(maneuverFeatures, null);
            for (TrainableSingleManeuverOfflineClassifier classifier : classifiers) {
                try {
                    classifier.loadPersistedModel();
                    allBoatClassesClassifiers.add(classifier);
                } catch (ClassifierPersistenceException e) {
                    e.printStackTrace();
                }
            }
            for (BoatClass boatClass : allBoatClasses) {
                classifiers = ManeuverClassifiersFactory.getAllTrainableClassifierInstances(maneuverFeatures,
                        boatClass);
                for (TrainableSingleManeuverOfflineClassifier classifier : classifiers) {
                    try {
                        classifier.loadPersistedModel();
                        allBoatClassesClassifiers.add(classifier);
                    } catch (ClassifierPersistenceException e) {
                    }
                }
            }
        }
        StringBuilder str = new StringBuilder("Classifier name \t| Maneuver features \t| Boat class \t| Test score");
        Collections.sort(allBoatClassesClassifiers, (a, b) -> Double.compare(a.getTestScore(), b.getTestScore()));
        for (TrainableSingleManeuverOfflineClassifier classifier : allBoatClassesClassifiers) {
            str.append("\r\n" + classifier.getClass().getSimpleName() + ": \t| " + classifier.getManeuverFeatures()
                    + " \t| " + classifier.getBoatClass() + " \t| "
                    + String.format(" %.03f", classifier.getTestScore()));
        }
        String outputStr = str.toString();
        LoggingUtil.logInfo(outputStr);
        outputStr = outputStr.replaceAll(Pattern.quote(" \t| "), ";");
        Files.write(Paths.get("classifierScores.csv"), outputStr.getBytes());
    }

}
