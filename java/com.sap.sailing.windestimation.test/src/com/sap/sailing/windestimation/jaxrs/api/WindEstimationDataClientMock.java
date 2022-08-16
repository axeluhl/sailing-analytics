package com.sap.sailing.windestimation.jaxrs.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.json.simple.parser.ParseException;

import com.sap.sailing.windestimation.integration.ExportedModels;
import com.sap.sailing.windestimation.integration.ReplicableWindEstimationFactoryService;
import com.sap.sailing.windestimation.jaxrs.client.WindEstimationDataClient;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class WindEstimationDataClientMock extends WindEstimationDataClient {

    private final ModelStore modelStore;

    public WindEstimationDataClientMock(ModelStore modelStore,
            ReplicableWindEstimationFactoryService windEstimationFactoryService) {
        super(null, windEstimationFactoryService);
        this.modelStore = modelStore;
    }

    @Override
    protected InputStream getContentFromResponse() throws IOException, ParseException {
        ExportedModels exportedModels = new ExportedModels();
        for (ModelDomainType domainType : ModelDomainType.values()) {
            Map<String, byte[]> exportedModelsForDomainType = modelStore.exportAllPersistedModels(domainType);
            exportedModels.addSerializedModelsForDomainType(domainType, exportedModelsForDomainType);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
            oos.writeObject(exportedModels);
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

}
