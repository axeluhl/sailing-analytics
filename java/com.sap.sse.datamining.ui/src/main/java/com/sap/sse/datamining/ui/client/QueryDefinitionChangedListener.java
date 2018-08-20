package com.sap.sse.datamining.ui.client;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public interface QueryDefinitionChangedListener {

    void queryDefinitionChanged(StatisticQueryDefinitionDTO newQueryDefinition);

}
