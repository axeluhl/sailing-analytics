package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface QueryDefinitionProvider extends Component<AbstractSettings>  {

    public Iterable<String> validateQueryDefinition(QueryDefinitionDTO queryDefinition);
    public QueryDefinitionDTO getQueryDefinition();
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

}
