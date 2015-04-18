package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;

public interface QueryDefinitionChangedListener {

    void queryDefinitionChanged(QueryDefinitionDTO newQueryDefinition);

}
