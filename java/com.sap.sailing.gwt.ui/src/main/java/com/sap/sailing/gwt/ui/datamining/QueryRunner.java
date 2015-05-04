package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;
import com.sap.sse.datamining.shared.dto.QueryDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface QueryRunner extends QueryDefinitionChangedListener, Component<QueryRunnerSettings> {
    
    public void run(QueryDefinitionDTO queryDefinition);

}
