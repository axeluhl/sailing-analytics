package com.sap.sse.datamining.ui.client;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionIdentifier;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.ui.client.selection.DimensionFilterSelectionProvider;
import com.sap.sse.datamining.ui.client.selection.QueryDefinitionProviderWithControls;

/**
 * Tracks the use of {@link FilterDimensionParameter}s in the scope of a {@link QueryDefinitionProviderWithControls
 * query editor} with its {@link DimensionFilterSelectionProvider} components where dimension filters can be bound to
 * report parameters.<p>
 * 
 * The query editor is not constantly maintaining a {@link StatisticQueryDefinitionDTO query object} but can only
 * be filled from one and can produce a new one based on its UI state. Therefore, the parameter binding model as
 * expressed in a {@link DataMiningReportDTO} cannot easily be used here. Instead, we track parameter bindings
 * in the UI in objects of this class and convert them to real parameter usage records when the query is
 * inserted into a report. Conversely, when filling the UI from a query that comes from a report, the parameter
 * binding information is converted into an object of this type, tracked by this object while editing the query
 * and transformed back into the report's parameter usage model when running the query successfully and hence
 * updating the report accordingly.
 */
public class ReportParameterToDimensionFilterBindings {
    private final Map<FilterDimensionIdentifier, FilterDimensionParameter> parameterBindings;

    public ReportParameterToDimensionFilterBindings() {
        this(new HashMap<>());
    }
    
    public ReportParameterToDimensionFilterBindings(
            Map<FilterDimensionIdentifier, FilterDimensionParameter> parameterBindings) {
        this.parameterBindings = new HashMap<>(parameterBindings);
    }
    
    public ReportParameterToDimensionFilterBindings(DataMiningReportDTO report, StatisticQueryDefinitionDTO query) {
        this(fillFromReportAndQuery(report, query));
    }
    
    private static Map<FilterDimensionIdentifier, FilterDimensionParameter> fillFromReportAndQuery(DataMiningReportDTO report, StatisticQueryDefinitionDTO query) {
        final Map<FilterDimensionIdentifier, FilterDimensionParameter> result = new HashMap<>();
        for (final Pair<FilterDimensionIdentifier, FilterDimensionParameter> usage : report.getParameterUsages(query)) {
            result.put(usage.getA(), usage.getB());
        }
        return result;
    }
    
    public FilterDimensionParameter getParameterBinding(FilterDimensionIdentifier filterDimensionIdentifier) {
        return parameterBindings.get(filterDimensionIdentifier);
    }
    
    public void setParameterBinding(FilterDimensionIdentifier filterDimensionIdentifier, FilterDimensionParameter parameter) {
        parameterBindings.put(filterDimensionIdentifier, parameter);
    }
    
    public void removeParameterBinding(FilterDimensionIdentifier filterDimensionIdentifier) {
        parameterBindings.remove(filterDimensionIdentifier);
    }
}
