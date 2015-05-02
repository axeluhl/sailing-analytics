package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public interface QueryDefinitionProvider extends DataMiningComponentProvider  {

    public Iterable<String> validateQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);
    public StatisticQueryDefinitionDTO getQueryDefinition();
    
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

}
