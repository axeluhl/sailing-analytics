package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.gwt.ui.client.shared.components.Component;

public interface QueryDefinitionProvider extends Component<Object>  {

    public Iterable<String> validateQueryDefinition(QueryDefinitionDeprecated queryDefinition);
    public QueryDefinitionDeprecated getQueryDefinition();
    
    public void applyQueryDefinition(QueryDefinitionDeprecated queryDefinition);

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

    public abstract SelectionProvider<?> getSelectionProvider();

}
