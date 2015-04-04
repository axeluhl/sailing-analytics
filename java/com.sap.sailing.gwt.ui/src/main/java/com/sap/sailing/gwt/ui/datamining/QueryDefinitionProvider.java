package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;

public interface QueryDefinitionProvider extends Component<Object>  {

    public Iterable<String> validateQueryDefinition(QueryDefinitionDTO queryDefinition);
    public QueryDefinitionDTO getQueryDefinition();
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

}
