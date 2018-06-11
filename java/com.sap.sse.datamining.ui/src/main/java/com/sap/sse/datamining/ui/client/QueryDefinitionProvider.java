package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public interface QueryDefinitionProvider<SettingsType extends Settings>
        extends DataMiningComponentProvider<SettingsType> {

    public Iterable<String> validateQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

    public StatisticQueryDefinitionDTO getQueryDefinition();

    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

}
