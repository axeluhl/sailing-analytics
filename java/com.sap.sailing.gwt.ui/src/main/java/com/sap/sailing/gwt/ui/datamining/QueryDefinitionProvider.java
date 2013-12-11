package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.QueryDefinition;

public interface QueryDefinitionProvider  {

    public Iterable<String> validateQueryDefinition(QueryDefinition queryDefinition);
    public QueryDefinition getQueryDefinition();
    public void applyQueryDefinition(QueryDefinition queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

    public Widget getWidget();
    
    public abstract SelectionProvider<?> getSelectionProvider();

}
