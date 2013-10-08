package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.QueryDefinition;

public interface QueryComponentsProvider<DimensionType> {

    public Iterable<String> validateQueryDefinition(QueryDefinition<DimensionType> queryDefinition);
    public QueryDefinition<DimensionType> getQueryDefinition();

    public void addListener(QueryComponentsChangedListener<DimensionType> listener);
    public void removeListener(QueryComponentsChangedListener<DimensionType> listener);

    public Widget getWidget();

}
