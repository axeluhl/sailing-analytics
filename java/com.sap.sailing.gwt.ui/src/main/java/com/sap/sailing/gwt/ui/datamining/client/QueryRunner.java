package com.sap.sailing.gwt.ui.datamining.client;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.datamining.client.settings.QueryRunnerSettings;

public interface QueryRunner extends QueryDefinitionChangedListener, Component<QueryRunnerSettings> {
    
    public void run(QueryDefinition queryDefinition);

}
