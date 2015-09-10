package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public interface DataRetrieverChainDefinitionProvider extends DataMiningComponentProvider {
    
    public DataRetrieverChainDefinitionDTO<Settings> getDataRetrieverChainDefinition();

    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener);
    
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

}
