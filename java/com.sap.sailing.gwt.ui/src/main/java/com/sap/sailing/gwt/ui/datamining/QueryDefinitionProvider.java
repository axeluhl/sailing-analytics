package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.QueryDefinition;

public interface QueryDefinitionProvider<DimensionType> {

    public Iterable<String> validateQueryDefinition(QueryDefinition<DimensionType> queryDefinition);
    public QueryDefinition<DimensionType> getQueryDefinition();

    public void addListener(QueryDefinitionChangedListener<DimensionType> listener);
    public void removeListener(QueryDefinitionChangedListener<DimensionType> listener);

    public Widget getWidget();

}
