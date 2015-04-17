package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface GroupingProvider extends Component<AbstractSettings> {
    
    public Collection<FunctionDTO> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);
    
    public void addGroupingChangedListener(GroupingChangedListener listener);

}
