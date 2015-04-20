package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;

public interface QueryDefinitionProvider extends DataMiningComponentProvider  {

    public Iterable<String> validateQueryDefinition(QueryDefinitionDTO queryDefinition);
    public QueryDefinitionDTO getQueryDefinition();
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

}
