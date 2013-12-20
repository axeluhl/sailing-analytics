package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.gwt.ui.client.shared.components.Component;

public interface GroupingProvider extends Component<Object> {
    
    public GrouperType getGrouperType();
    
    public Collection<SharedDimension> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();
    
    public void applyQueryDefinition(QueryDefinition queryDefinition);
    
    public void addGroupingChangedListener(GroupingChangedListener listener);

}
