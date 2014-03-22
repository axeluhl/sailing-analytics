package com.sap.sailing.gwt.ui.datamining.client;

import java.util.Collection;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.datamining.shared.components.GrouperType;

public interface GroupingProvider extends Component<Object> {
    
    public GrouperType getGrouperType();
    
    public Collection<DimensionIdentifier> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();
    
    public void applyQueryDefinition(QueryDefinition queryDefinition);
    
    public void addGroupingChangedListener(GroupingChangedListener listener);

}
