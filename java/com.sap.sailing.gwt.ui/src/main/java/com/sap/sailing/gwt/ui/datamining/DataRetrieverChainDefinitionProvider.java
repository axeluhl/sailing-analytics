package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public interface DataRetrieverChainDefinitionProvider extends DataMiningComponentProvider, StatisticChangedListener {
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();

    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

}
