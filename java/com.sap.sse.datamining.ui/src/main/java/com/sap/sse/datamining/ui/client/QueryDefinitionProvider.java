package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface QueryDefinitionProvider<SettingsType extends Settings> extends Component<SettingsType> {

    void reloadComponents();

    StatisticQueryDefinitionDTO getQueryDefinition();
    Iterable<String> validateQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);
    void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);
    
    void queryDefinitionChangesHaveBeenStored();

    void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
    void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

}
