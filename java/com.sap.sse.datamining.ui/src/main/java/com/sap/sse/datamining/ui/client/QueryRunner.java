package com.sap.sse.datamining.ui.client;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.ui.client.settings.QueryRunnerSettings;
import com.sap.sse.gwt.client.shared.components.Component;

public interface QueryRunner extends QueryDefinitionChangedListener, Component<QueryRunnerSettings> {

    void run(StatisticQueryDefinitionDTO queryDefinition);

    void runQuery();

}
