package com.sap.sse.datamining.shared.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionIdentifier;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

/**
 * Tracks the use of {@link FilterDimensionParameter}s in the scope of a {@code QueryDefinitionProviderWithControls}
 * query editor with its {@code DimensionFilterSelectionProvider} components where dimension filters can be bound to
 * report parameters. Instances of this class are mutable, and a single instance is assumed to represent the temporary
 * parameter binding information of a {@code QueryDefinitionProvider} before that provider is asked to
 * {@link QueryDefinitionProvider#getQueryDefinition() produce its query}.
 * <p>
 * 
 * The query editor is not constantly maintaining a {@link StatisticQueryDefinitionDTO query object} but can only
 * be filled from one and can produce a new one based on its UI state. Therefore, the parameter binding model as
 * expressed in a {@link DataMiningReportDTO} cannot easily be used here. Instead, we track parameter bindings
 * in the UI in objects of this class and convert them to real parameter usage records when the query is
 * inserted into a report. Conversely, when filling the UI from a query that comes from a report, the parameter
 * binding information is converted into an object of this type, tracked by this object while editing the query
 * and transformed back into the report's parameter usage model when running the query successfully and hence
 * updating the report accordingly.
 * 
 * @see DataMiningReportDTO#getParameterUsages(StatisticQueryDefinitionDTO)
 */
public class ReportParameterToDimensionFilterBindings implements Iterable<Entry<FilterDimensionIdentifier, FilterDimensionParameter>> {
    private final Map<FilterDimensionIdentifier, FilterDimensionParameter> parameterBindings;

    public ReportParameterToDimensionFilterBindings() {
        this(new HashMap<>());
    }
    
    public ReportParameterToDimensionFilterBindings(
            Map<FilterDimensionIdentifier, FilterDimensionParameter> parameterBindings) {
        this.parameterBindings = new HashMap<>(parameterBindings);
    }
    
    /**
     * Copy constructor
     */
    public ReportParameterToDimensionFilterBindings(ReportParameterToDimensionFilterBindings other) {
        this();
        fillFromOther(other);
    }

    private void fillFromOther(ReportParameterToDimensionFilterBindings other) {
        for (final Entry<FilterDimensionIdentifier, FilterDimensionParameter> e : other) {
            setParameterBinding(e.getKey(), e.getValue());
        }
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

    @Override
    public Iterator<Entry<FilterDimensionIdentifier, FilterDimensionParameter>> iterator() {
        return parameterBindings.entrySet().iterator();
    }

    public void set(ReportParameterToDimensionFilterBindings parameterUsages) {
        parameterBindings.clear();
        fillFromOther(parameterUsages);
    }
    
    @Override
    public String toString() {
        return parameterBindings.toString();
    }
}
