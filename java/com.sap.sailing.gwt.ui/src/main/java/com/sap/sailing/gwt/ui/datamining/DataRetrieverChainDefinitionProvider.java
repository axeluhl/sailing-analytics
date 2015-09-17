package com.sap.sailing.gwt.ui.datamining;

import java.util.HashMap;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;

public interface DataRetrieverChainDefinitionProvider extends DataMiningComponentProvider {
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();

    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener);
    
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

    // TODO This is just a quick fix. Delete, after the settings have been improved.
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings();

}
