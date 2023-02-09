package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.stream.StreamSupport;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.parameters.ParameterModelListener;

/**
 * Manages a sequence of {@link ModifiableStatisticQueryDefinitionDTO queries} and a model of
 * {@link FilterDimensionParameter parameters} used by those queries. The queries are unaware of parameters. It is this
 * object keeping track of the binding. Usages of a parameter in a query must be managed explicitly by invoking
 * {@link #addParameterUsage(StatisticQueryDefinitionDTO, FilterDimensionParameter, FilterDimensionIdentifier)} and
 * {@link #removeParameterUsage(StatisticQueryDefinitionDTO, FilterDimensionParameter, FilterDimensionIdentifier)}.
 * If a parameter is {@link #removeParameter(FilterDimensionParameter) removed} altogether, its usages are cleared
 * implicitly.<p>
 * 
 * All parameters {@link #createParameter(String, String, Iterable) created} for this report are observed for
 * changes in their {@link FilterDimensionParameter#getValues() value set}, and if it changes, all queries in this
 * reports that are using it will have their dimension filter bound to the parameter updated with the new parameter
 * value.
 */
public interface DataMiningReportDTO extends Serializable {

    Iterable<FilterDimensionParameter> getParameters();

    FilterDimensionParameter createParameter(String name, String typeName, Iterable<? extends Serializable> values);

    /**
     * Removes the parameter from this reports, together with all its usages.
     */
    void removeParameter(FilterDimensionParameter parameter);

    Iterable<StatisticQueryDefinitionDTO> getQueryDefinitions();
    
    /**
     * If the query is not yet part of this report, the query is added. Otherwise, it simply remains part
     * of this report, and this method does nothing.
     */
    void addQueryDefinition(ModifiableStatisticQueryDefinitionDTO queryDefinition);
    
    /**
     * Removes the query definition from this reports and adjustes the
     * {@link #getParameterUsages(FilterDimensionParameter) parameter usages} accordingly, removing all usages within
     * the query removed.
     */
    boolean removeQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);
    
    /**
     * Records a parameter's usage in one of this report's {@link #getQueryDefinitions() queries}. The usage will then
     * be reported accordingly in {@link #getParameterUsages(FilterDimensionParameter)} until the usage is
     * {@link #removeParameterUsage removed} again.
     * @param query
     *            a query from {@link #getQueryDefinitions()}
     * @param parameter
     *            a parameter from {@link #getParameters()}
     * @param filterDimensionIdentifier
     *            a {@link FilterDimensionIdentifier#getRetrieverLevel() retriever level} that occurs in the
     *            {@link StatisticQueryDefinitionDTO#getDataRetrieverChainDefinition() query's retriever chain} and a
     *            {@link FilterDimensionIdentifier#getDimensionFunction() dimension} for which a
     *            {@link StatisticQueryDefinitionDTO#getFilterSelection() filter} exists in the query.
     */
    void addParameterUsage(StatisticQueryDefinitionDTO query, FilterDimensionParameter parameter,
            FilterDimensionIdentifier filterDimensionIdentifier);

    void removeParameterUsage(StatisticQueryDefinitionDTO query, FilterDimensionParameter parameter,
            FilterDimensionIdentifier filterDimensionIdentifier);

    Iterable<Pair<StatisticQueryDefinitionDTO, FilterDimensionIdentifier>> getParameterUsages(FilterDimensionParameter parameter);

    Iterable<Pair<FilterDimensionIdentifier, FilterDimensionParameter>> getParameterUsages(StatisticQueryDefinitionDTO query);

    FilterDimensionParameter getUsedParameter(StatisticQueryDefinitionDTO query, FilterDimensionIdentifier filterDimensionIdentifier);

    /**
     * Filters all {@link #getParameters() parameters} defined in this report by their
     * {@link FilterDimensionParameter#getTypeName() type name}. This shall allow a caller to find appropriate existing
     * parameters that may be used for binding them to a dimension filter.
     * 
     * @return a one-time iterable, never {@code null} but possibly empty
     */
    default Iterable<FilterDimensionParameter> getParametersForTypeName(final String typeName) {
        return StreamSupport.stream(getParameters().spliterator(), /* parallel */ false)
                .filter(parameter -> Util.equalsWithNull(parameter.getTypeName(), typeName))::iterator;
    }

    void addParamterModelListener(ParameterModelListener listener);

    void removeParameterModelListener(ParameterModelListener listener);
}
