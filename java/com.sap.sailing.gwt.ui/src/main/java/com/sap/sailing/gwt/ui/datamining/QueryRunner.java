package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.datamining.settings.QueryRunnerSettings;

public interface QueryRunner extends QueryDefinitionChangedListener, Component<QueryRunnerSettings> {
    
    public void run(QueryDefinitionDeprecated queryDefinition);

}
