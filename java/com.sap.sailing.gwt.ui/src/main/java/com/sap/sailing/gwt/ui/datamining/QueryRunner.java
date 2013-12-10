package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.datamining.settings.DataMiningSettings;

public interface QueryRunner extends QueryDefinitionChangedListener, Component<DataMiningSettings> {
    
    public void run(QueryDefinition queryDefinition);

}
