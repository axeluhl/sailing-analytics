package com.sap.sse.datamining.shared.impl.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.dto.DataMiningReportDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionIdentifier;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public class ModifiableDataMiningReportDTO implements DataMiningReportDTO {
    private static final long serialVersionUID = -6512175470789118223L;
    
    private ArrayList<StatisticQueryDefinitionDTO> queryDefinitions;
    private HashSet<FilterDimensionParameter> parameters;
    private HashMap<StatisticQueryDefinitionDTO, HashMap<FilterDimensionIdentifier, FilterDimensionParameter>> parameterUsages;

    /**
     * Creates an empty report with 
     */
    public ModifiableDataMiningReportDTO() {
        this(Collections.emptySet(), Collections.emptySet());
    }
    
    public ModifiableDataMiningReportDTO(Iterable<StatisticQueryDefinitionDTO> queryDefinitions, Iterable<FilterDimensionParameter> parameters) {
        this.queryDefinitions = new ArrayList<>();
        Util.addAll(queryDefinitions, this.queryDefinitions);
        this.parameters = new HashSet<>();
        Util.addAll(parameters, this.parameters);
    }

    @Override
    public Iterable<StatisticQueryDefinitionDTO> getQueryDefinitions() {
        return queryDefinitions;
    }
    
    public void addQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        queryDefinitions.add(queryDefinition);
    }
    
    /**
     * Removes the query definition from this reports and adjustes the {@link #parameterUsages} accordingly,
     * removing all usages within the query removed.
     */
    public boolean removeQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        parameterUsages.remove(queryDefinition);
        return queryDefinitions.remove(queryDefinition);
    }

    @Override
    public Iterable<FilterDimensionParameter> getParameters() {
        return parameters;
    }

    @Override
    public Iterable<Pair<StatisticQueryDefinitionDTO, FilterDimensionIdentifier>> getParameterUsages(FilterDimensionParameter parameter) {
        final List<Pair<StatisticQueryDefinitionDTO, FilterDimensionIdentifier>> result = new ArrayList<>();
        for (final Entry<StatisticQueryDefinitionDTO, HashMap<FilterDimensionIdentifier, FilterDimensionParameter>> e : parameterUsages.entrySet()) {
            for (final FilterDimensionIdentifier f : e.getValue().keySet()) {
                result.add(new Pair<>(e.getKey(), f));
            }
        }
        return result;
    }

    @Override
    public FilterDimensionParameter getParameter(StatisticQueryDefinitionDTO query, FilterDimensionIdentifier filterDimensionIdentifier) {
        final FilterDimensionParameter result;
        final HashMap<FilterDimensionIdentifier, FilterDimensionParameter> parameterUsagesInQuery = parameterUsages.get(query);
        if (parameterUsagesInQuery != null) {
            result = parameterUsagesInQuery.get(filterDimensionIdentifier);
        } else {
            result = null;
        }
        return result;
    }

}
