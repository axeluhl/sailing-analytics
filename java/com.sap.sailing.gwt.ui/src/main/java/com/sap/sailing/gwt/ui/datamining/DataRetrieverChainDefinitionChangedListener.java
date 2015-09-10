package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public interface DataRetrieverChainDefinitionChangedListener {
    
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO<SerializableSettings> dataRetrieverChainDefinitionDTO);

}
