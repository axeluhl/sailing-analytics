package com.sap.sailing.windestimation.evaluation;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.util.Map;

import com.mongodb.client.MongoDatabase;
import com.sap.sailing.windestimation.data.persistence.maneuver.RaceWithCompleteManeuverCurvePersistenceManager;
import com.sap.sailing.windestimation.integration.ExportedModels;
import com.sap.sailing.windestimation.integration.WindEstimationFactoryServiceImpl;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;

/**
 * Imports already trained wind estimation models as a exported file in the specified {@link ModelStore} and makes these
 * models available for evaluation framework.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationModelsForEvaluationImporter {

    private static final String exportedWindEstimationModelsFilePath = "windestimation_data_axel";

    private static final ModelStore MODEL_STORE = new MongoDbModelStoreImpl(getDb());

    public static void main(String[] args) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(exportedWindEstimationModelsFilePath))) {
            ExportedModels exportedModels = (ExportedModels) ois.readObject();
            for (ModelDomainType domainType : WindEstimationFactoryServiceImpl.modelDomainTypesRequiredByWindEstimation) {
                Map<String, byte[]> serializedModelsForDomainType = exportedModels
                        .getSerializedModelsForDomainType(domainType);
                if (serializedModelsForDomainType != null) {
                    MODEL_STORE.deleteAll(domainType);
                    MODEL_STORE.importPersistedModels(serializedModelsForDomainType, domainType);
                    LoggingUtil.logInfo("Imported " + domainType + " models");
                }
            }
        }
        LoggingUtil.logInfo("Model import finished");
    }

    private static MongoDatabase getDb() {
        try {
            return new RaceWithCompleteManeuverCurvePersistenceManager().getDb();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
