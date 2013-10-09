package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.datamining.shared.QueryDefinition;

public interface QueryDefinitionChangedListener<DimensionType> {

	void queryDefinitionChanged(QueryDefinition<DimensionType> newQueryDefinition);

}
