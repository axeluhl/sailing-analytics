package com.sap.sse.datamining.ui.client;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.data.ReportParameterToDimensionFilterBindings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface QueryDefinitionProvider<SettingsType extends Settings> extends Component<SettingsType> {
    void reloadComponents();

    /**
     * Constructs a new query based on the aggregator, statistic, grouping and dimension filter specifications
     */
    StatisticQueryDefinitionDTO getQueryDefinition();
    
    Pair<ModifiableStatisticQueryDefinitionDTO, ReportParameterToDimensionFilterBindings> getQueryDefinitionAndReportParameterBinding();
    
    /**
     * Checks whether a valid non-{@code null} statistic selection, aggregator selection, and grouping definition exist.
     * Otherwise, localized error messages describing the respective problem(s) are returned.
     * 
     * @return a non-{@code null} but possibly empty sequence of error messages
     */
    Iterable<String> validateQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

    void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

    void queryDefinitionChangesHaveBeenStored();

    void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);

    void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener);
}
