package com.sap.sailing.windestimation.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.integration.ExportedModels;
import com.sap.sailing.windestimation.model.store.FileSystemModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class ExportedModelsGenerator {

    private static final File file = new File("windEstimationModels.dat");

    public static void main(String[] args) throws FileNotFoundException, IOException {
        ModelStore modelStore = new FileSystemModelStoreImpl("trained_wind_estimation_models");
        // RegularManeuversForEstimationPersistenceManager persistenceManager = new
        // RegularManeuversForEstimationPersistenceManager();
        // ModelStore modelStore = new MongoDbModelStoreImpl(persistenceManager.getDb());
        LoggingUtil.logInfo("Generation a single file with all trained models started");
        ExportedModels exportedModels = new ExportedModels();
        List<ModelDomainType> nonEmptyDomainTypes = new ArrayList<>();
        for (ModelDomainType domainType : ModelDomainType.values()) {
            LoggingUtil.logInfo("Started serialization of models belonging to domain type " + domainType);
            Map<String, byte[]> exportedModelsForDomainType = modelStore.exportAllPersistedModels(domainType);
            if (!exportedModelsForDomainType.isEmpty()) {
                exportedModels.addSerializedModelsForDomainType(domainType, exportedModelsForDomainType);
                LoggingUtil.logInfo("Finished serialization of " + exportedModelsForDomainType.size()
                        + " models belonging to domain type " + domainType);
                nonEmptyDomainTypes.add(domainType);
            }
        }
        LoggingUtil.logInfo("Serializing the exported models and writing it into a single file");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(exportedModels);
        }
        LoggingUtil.logInfo("Models have been generated");
        LoggingUtil.logInfo("The following domain types were considered: " + nonEmptyDomainTypes);
        LoggingUtil.logInfo("File with all exported models was persisted in: " + file.getAbsolutePath());
    }

}
