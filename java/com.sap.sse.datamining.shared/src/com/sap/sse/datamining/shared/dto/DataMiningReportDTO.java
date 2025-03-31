package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.data.ReportParameterToDimensionFilterBindings;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.parameters.ParameterModelListener;

/**
 * Manages a sequence of {@link ModifiableStatisticQueryDefinitionDTO queries} and a model of
 * {@link FilterDimensionParameter parameters} used by those queries. The queries are unaware of parameters. It is this
 * object keeping track of the binding. Usages of a parameter in a query must be managed explicitly by invoking
 * {@link #addParameterUsage(StatisticQueryDefinitionDTO, FilterDimensionIdentifier, FilterDimensionParameter)} and
 * {@link #removeParameterUsage(StatisticQueryDefinitionDTO, FilterDimensionIdentifier, FilterDimensionParameter)}.
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
    default void addQueryDefinition(ModifiableStatisticQueryDefinitionDTO queryDefinition) {
        addQueryDefinition(Util.size(getQueryDefinitions()), queryDefinition);
    }
    
    /**
     * If the query is not yet part of this report, the query is added at the index specified. Otherwise, it simply
     * remains part of this report, regardless its current position, and this method does nothing.
     * 
     * @param index
     *            must be between 0 (inclusive) and the number of {@link #getQueryDefinitions() queries} currently in
     *            this report (exclusive)
     * @throws IndexOutOfBoundsException
     *             if {@code index} is negative or greater or equals to the current number of
     *             {@link #getQueryDefinitions() queries} in this report
     */
    void addQueryDefinition(int index, ModifiableStatisticQueryDefinitionDTO queryDefinition);
    
    /**
     * If the {@code toReplace} query is found, it is replaced by {@code replaceWith} in this report's
     * {@link #getQueryDefinitions() query sequence}. Otherwise, {@code replaceWith} is appended to its end.
     * The parameter usages are recorded if {@code parameterBindings} is not {@code null}.
     * 
     * @param toReplace
     *            may be {@code null} in case no query is being replaced by {@code replaceWith}; otherwise, assumed to
     *            be in {@link #getQueryDefinitions()} and will be removed before adding {@code replaceWith} at the same
     *            index in the query definitions sequence.
     * @param parameterBindings
     *            may be {@code null}; if not {@code null}, the parameter bindings described therein will be
     *            {@link #addParameterUsage(StatisticQueryDefinitionDTO, FilterDimensionIdentifier, FilterDimensionParameter)
     *            added} to this report as used by the {@code replaceWith} query.
     */
    default void replaceQueryDefinition(StatisticQueryDefinitionDTO toReplace,
            ModifiableStatisticQueryDefinitionDTO replaceWith,
            ReportParameterToDimensionFilterBindings parameterBindings) {
        final int index;
        if (toReplace != null) {
            final int i;
            index = (i=removeQueryDefinition(toReplace)) < 0 ? Util.size(getQueryDefinitions()) : i;
        } else {
            index = Util.size(getQueryDefinitions());
        }
        addQueryDefinition(index, replaceWith);
        if (parameterBindings != null) {
            for (final Entry<FilterDimensionIdentifier, FilterDimensionParameter> e : parameterBindings) {
                addParameterUsage(replaceWith, e.getKey(), e.getValue());
            }
        }
    }
    
    /**
     * Removes the query definition from this reports and adjustes the
     * {@link #getParameterUsages(FilterDimensionParameter) parameter usages} accordingly, removing all usages within
     * the query removed.
     * 
     * @return the index into {@link #getQueryDefinitions()} where the {@code queryDefinition} was found, or -1 if the
     *         query was not found
     */
    int removeQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);
    
    /**
     * Records a parameter's usage in one of this report's {@link #getQueryDefinitions() queries}. The usage will then
     * be reported accordingly in {@link #getParameterUsages(FilterDimensionParameter)} until the usage is
     * {@link #removeParameterUsage removed} again.
     * @param query
     *            a query from {@link #getQueryDefinitions()}
     * @param filterDimensionIdentifier
     *            a {@link FilterDimensionIdentifier#getRetrieverLevel() retriever level} that occurs in the
     *            {@link StatisticQueryDefinitionDTO#getDataRetrieverChainDefinition() query's retriever chain} and a
     *            {@link FilterDimensionIdentifier#getDimensionFunction() dimension} for which a
     *            {@link StatisticQueryDefinitionDTO#getFilterSelection() filter} exists in the query.
     * @param parameter
     *            a parameter from {@link #getParameters()}
     */
    void addParameterUsage(StatisticQueryDefinitionDTO query, FilterDimensionIdentifier filterDimensionIdentifier,
            FilterDimensionParameter parameter);

    void removeParameterUsage(StatisticQueryDefinitionDTO query, FilterDimensionIdentifier filterDimensionIdentifier,
            FilterDimensionParameter parameter);

    Iterable<Pair<StatisticQueryDefinitionDTO, FilterDimensionIdentifier>> getParameterUsages(FilterDimensionParameter parameter);

    ReportParameterToDimensionFilterBindings getParameterUsages(StatisticQueryDefinitionDTO query);

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
