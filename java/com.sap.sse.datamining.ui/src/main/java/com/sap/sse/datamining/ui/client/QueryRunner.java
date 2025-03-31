package com.sap.sse.datamining.ui.client;

import com.sap.sse.datamining.shared.data.ReportParameterToDimensionFilterBindings;
import com.sap.sse.datamining.shared.dto.FilterDimensionIdentifier;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.ui.client.settings.QueryRunnerSettings;
import com.sap.sse.gwt.client.shared.components.Component;

public interface QueryRunner extends QueryDefinitionChangedListener, Component<QueryRunnerSettings> {
    /**
     * @param queryDefinition
     *            the query to run
     * @param reportParameterBindings
     *            either {@code null}, or a possibly empty set of bindings of {@link FilterDimensionParameter report
     *            parameters} to usages in the query in the form of {@link FilterDimensionIdentifier}s pointing to the
     *            retriever level and dimension function to identify the place of the parameter use
     */
    void run(ModifiableStatisticQueryDefinitionDTO queryDefinition, ReportParameterToDimensionFilterBindings reportParameterBindings);
}
