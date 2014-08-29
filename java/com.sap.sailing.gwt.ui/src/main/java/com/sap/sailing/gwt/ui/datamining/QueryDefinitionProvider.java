package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.datamining.shared.QueryDefinition;

public interface QueryDefinitionProvider extends Component<Object>  {

    public Iterable<String> validateQueryDefinition(QueryDefinition queryDefinition);
    public QueryDefinition getQueryDefinition();
    
    public void applyQueryDefinition(QueryDefinition queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

    public abstract SelectionProvider<?> getSelectionProvider();

}
