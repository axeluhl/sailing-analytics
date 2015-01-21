package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.QueryDefinitionDTO;

public interface QueryDefinitionChangedListener {

    void queryDefinitionChanged(QueryDefinitionDTO newQueryDefinition);

}
