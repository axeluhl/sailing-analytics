package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface GroupingProvider extends DataMiningComponentProvider, StatisticChangedListener{
    
    public Collection<FunctionDTO> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);
    
    public void addGroupingChangedListener(GroupingChangedListener listener);

}
