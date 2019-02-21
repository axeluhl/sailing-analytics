package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.windestimation.data.LabeledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.persistence.maneuver.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.TransformedManeuversPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.Util.Pair;

public class ManeuverClassifierTrainer {
    private static final int MIN_MANEUVERS_COUNT = 500;

    private final TransformedManeuversPersistenceManager<LabeledManeuverForEstimation> persistenceManager;
    private List<LabeledManeuverForEstimation> allManeuvers;
    private Map<Pair<String, ManeuverFeatures>, List<LabeledManeuverForEstimation>> maneuversPerBoatClass = new HashMap<>();

    private final ModelStore classifierModelStore;

    public ManeuverClassifierTrainer(
            TransformedManeuversPersistenceManager<LabeledManeuverForEstimation> persistenceManager,
            ModelStore classifierModelStore) {
        this.persistenceManager = persistenceManager;
        this.classifierModelStore = classifierModelStore;
    }

    public void trainClassifier(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel,
            LabelExtraction<LabeledManeuverForEstimation> labelExtraction) throws Exception {
        ManeuverClassifierModelContext modelContext = classifierModel.getModelContext();
        List<LabeledManeuverForEstimation> maneuvers = getSuitableManeuvers(modelContext);
        LoggingUtil.logInfo("Using " + maneuvers.size() + " maneuvers");
        if (maneuvers.size() < MIN_MANEUVERS_COUNT) {
            LoggingUtil.logInfo("Not enough maneuver data for training. Training aborted!");
        } else {
            LoggingUtil.logInfo("Splitting training and test data...");
            List<LabeledManeuverForEstimation> trainManeuvers = new ArrayList<>();
            List<LabeledManeuverForEstimation> testManeuvers = new ArrayList<>();
            for (LabeledManeuverForEstimation maneuver : maneuvers) {
                if (maneuver.getRegattaName().contains("2018")) {
                    testManeuvers.add(maneuver);
                } else {
                    trainManeuvers.add(maneuver);
                }
            }
            if (testManeuvers.size() < MIN_MANEUVERS_COUNT * 0.2 || trainManeuvers.size() < MIN_MANEUVERS_COUNT * 0.8) {
                LoggingUtil.logInfo("Not enough maneuver data for training. Training aborted!");
            } else {
                LoggingUtil.logInfo("Training with  " + trainManeuvers.size() + " maneuvers...");
                double[][] x = modelContext.getXMatrix(maneuvers);
                int[] y = labelExtraction.getYVector(maneuvers);
                classifierModel.train(x, y);
                LoggingUtil.logInfo("Training finished. Validating on train dataset...");
                ManeuverClassifierScoring classifierScoring = new ManeuverClassifierScoring(classifierModel);
                String printScoring = classifierScoring.printScoring(trainManeuvers, labelExtraction);
                LoggingUtil.logInfo("Training score:\n" + printScoring);
                double trainScore = classifierScoring.getLastAvgF1Score();
                int numberOfTrainingInstances = trainManeuvers.size();

                LoggingUtil.logInfo("Validating on test dataset with " + testManeuvers.size() + " maneuvers...");
                printScoring = classifierScoring.printScoring(testManeuvers, labelExtraction);
                LoggingUtil.logInfo("Test score:\n" + printScoring);
                double testScore = classifierScoring.getLastAvgF1Score();
                LoggingUtil.logInfo("Persisting trained classifier...");
                classifierModel.setStatsAfterSuccessfulTraining(trainScore, testScore, numberOfTrainingInstances);
                classifierModelStore.persistModel(classifierModel);
                LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
            }
        }
    }

    private List<LabeledManeuverForEstimation> getSuitableManeuvers(ManeuverClassifierModelContext modelContext)
            throws JsonDeserializationException, ParseException {
        String boatClassName = modelContext.getBoatClassName();
        ManeuverFeatures maneuverFeatures = modelContext.getManeuverFeatures();
        Pair<String, ManeuverFeatures> key = new Pair<>(boatClassName, maneuverFeatures);
        List<LabeledManeuverForEstimation> maneuvers = maneuversPerBoatClass.get(key);
        if (maneuvers == null) {
            if (allManeuvers == null) {
                LoggingUtil.logInfo("Connecting to MongoDB");
                persistenceManager.createIfNotExistsCollectionWithTransformedManeuvers();
                LoggingUtil.logInfo("Querying dataset...");
                allManeuvers = persistenceManager.getAllElements();
            }
            LoggingUtil
                    .logInfo("Filtering maneuvers for boat class: " + (boatClassName == null ? "All" : boatClassName));
            maneuvers = allManeuvers.stream().filter(maneuver -> modelContext.isContainsAllFeatures(maneuver))
                    .collect(Collectors.toList());
            maneuversPerBoatClass.put(key, maneuvers);
        }
        return maneuvers;
    }

    public static void main(String[] args) throws Exception {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        RegularManeuversForEstimationPersistenceManager persistenceManager = new RegularManeuversForEstimationPersistenceManager();
        ModelStore classifierModelStore = new MongoDbModelStoreImpl(persistenceManager.getDb());
        classifierModelStore.deleteAll(ModelDomainType.MANEUVER_CLASSIFIER);
        ManeuverClassifierTrainer classifierTrainer = new ManeuverClassifierTrainer(persistenceManager,
                classifierModelStore);
        ManeuverClassifierModelFactory classifierModelFactory = new ManeuverClassifierModelFactory();
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            LoggingUtil.logInfo(
                    "### Training classifier for all boat classes with maneuver features: " + maneuverFeatures);
            ManeuverClassifierModelContext modelContext = new ManeuverClassifierModelContext(maneuverFeatures, null,
                    ManeuverClassifierModelFactory.orderedSupportedTargetValues);
            ManeuverLabelExtraction labelExtraction = new ManeuverLabelExtraction(modelContext);
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel = classifierModelFactory
                    .getNewModel(modelContext);
            LoggingUtil.logInfo("## Classifier: " + classifierModel.getClass().getName());
            classifierTrainer.trainClassifier(classifierModel, labelExtraction);
        }
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();

        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            for (BoatClass boatClass : allBoatClasses) {
                LoggingUtil.logInfo("### Training classifier for boat class " + boatClass + " with maneuver features: "
                        + maneuverFeatures);
                ManeuverClassifierModelContext modelContext = new ManeuverClassifierModelContext(maneuverFeatures,
                        boatClass.getName(), ManeuverClassifierModelFactory.orderedSupportedTargetValues);
                ManeuverLabelExtraction labelExtraction = new ManeuverLabelExtraction(modelContext);
                TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel = classifierModelFactory
                        .getNewModel(modelContext);
                LoggingUtil.logInfo("## Classifier: " + classifierModel.getClass().getName());
                classifierTrainer.trainClassifier(classifierModel, labelExtraction);
            }
        }
    }

}
