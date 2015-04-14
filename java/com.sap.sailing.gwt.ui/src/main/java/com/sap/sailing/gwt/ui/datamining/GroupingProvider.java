package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sailing.domain.common.settings.Settings;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface GroupingProvider extends Component<Settings> {
    
    public Collection<FunctionDTO> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);
    
    public void addGroupingChangedListener(GroupingChangedListener listener);

}
