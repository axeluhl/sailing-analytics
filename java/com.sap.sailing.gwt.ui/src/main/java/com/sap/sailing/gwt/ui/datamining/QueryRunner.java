package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;

public interface QueryRunner extends QueryDefinitionChangedListener, Component<QueryRunnerSettings> {
    
    public void run(QueryDefinitionDTO queryDefinition);

}
