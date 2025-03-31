package com.sap.sailing.windestimation.integration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.windestimation.model.store.ModelDomainType;

public class ExportedModels implements Serializable {

    private static final long serialVersionUID = -2387862126027546463L;

    private final Map<ModelDomainType, Map<String, byte[]>> serializedModelsPerDomainType = new HashMap<>();

    public void addSerializedModelsForDomainType(ModelDomainType domainType,
            Map<String, byte[]> serializedModelPerPersistenceKey) {
        serializedModelsPerDomainType.put(domainType, serializedModelPerPersistenceKey);
    }

    public Map<String, byte[]> getSerializedModelsForDomainType(ModelDomainType domainType) {
        return serializedModelsPerDomainType.get(domainType);
    }

}
