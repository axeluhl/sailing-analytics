package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface GroupingProvider extends DataMiningComponentProvider, StatisticChangedListener{
    
    public Collection<FunctionDTO> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();
    
    public void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);
    
    public void addGroupingChangedListener(GroupingChangedListener listener);

}
