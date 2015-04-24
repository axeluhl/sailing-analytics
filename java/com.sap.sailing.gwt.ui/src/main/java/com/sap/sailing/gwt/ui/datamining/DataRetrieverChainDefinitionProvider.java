package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface DataRetrieverChainDefinitionProvider extends Component<AbstractSettings> {
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();

    public void addDataRetrieverChainDefinitionChangedListener(DataRetrieverChainDefinitionChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

}
