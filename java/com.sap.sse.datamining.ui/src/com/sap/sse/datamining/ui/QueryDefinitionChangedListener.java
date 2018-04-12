package com.sap.sse.datamining.ui;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public interface QueryDefinitionChangedListener {

    void queryDefinitionChanged(StatisticQueryDefinitionDTO newQueryDefinition);

}
