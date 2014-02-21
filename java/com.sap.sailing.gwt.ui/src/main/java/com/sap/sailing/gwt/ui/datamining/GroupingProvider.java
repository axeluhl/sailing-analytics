package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.datamining.components.GrouperType;

public interface GroupingProvider extends Component<Object> {
    
    public GrouperType getGrouperType();
    
    public Collection<DimensionIdentifier> getDimensionsToGroupBy();
    public String getCustomGrouperScriptText();
    
    public void applyQueryDefinition(QueryDefinition queryDefinition);
    
    public void addGroupingChangedListener(GroupingChangedListener listener);

}
