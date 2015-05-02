package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public interface QueryDefinitionChangedListener {

    void queryDefinitionChanged(StatisticQueryDefinitionDTO newQueryDefinition);

}
