package com.sap.sailing.gwt.ui.datamining.client;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.gwt.ui.client.shared.components.Component;

public interface QueryDefinitionProvider extends Component<Object>  {

    public Iterable<String> validateQueryDefinition(QueryDefinition queryDefinition);
    public QueryDefinition getQueryDefinition();
    public void applyQueryDefinition(QueryDefinition queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

    public abstract SelectionProvider<?> getSelectionProvider();

}
