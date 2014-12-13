package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public interface DataRetrieverChainDefinitionProvider extends Component<Object> {
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();

    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

}
