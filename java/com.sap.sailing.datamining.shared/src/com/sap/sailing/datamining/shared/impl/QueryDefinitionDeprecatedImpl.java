package com.sap.sailing.datamining.shared.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.components.GrouperType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class QueryDefinitionDeprecatedImpl implements QueryDefinitionDeprecated {
    private static final long serialVersionUID = 8408302866342877839L;

    private String localeInfoName;

    private FunctionDTO extractionFunction;
    private GrouperType grouperType;
    private AggregatorType aggregatorType;

    private List<DimensionIdentifier> dimensionsToGroupBy;
    private Map<DimensionIdentifier, Iterable<? extends Serializable>> selectionMappedByDimension;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryDefinitionDeprecatedImpl() { }

    public QueryDefinitionDeprecatedImpl(String localeInfoName, GrouperType grouperType, FunctionDTO extractionFunction, AggregatorType aggregatorType) {
        this.localeInfoName = localeInfoName;
        this.grouperType = grouperType;
        this.extractionFunction = extractionFunction;
        this.aggregatorType = aggregatorType;

        dimensionsToGroupBy = new ArrayList<DimensionIdentifier>();
        selectionMappedByDimension = new HashMap<DimensionIdentifier, Iterable<? extends Serializable>>();
    }
    
    @Override
    public String getLocaleInfoName() {
        return localeInfoName;
    }

    @Override
    public GrouperType getGrouperType() {
        return grouperType;
    }

    @Override
    public FunctionDTO getExtractionFunction() {
        return extractionFunction;
    }

    @Override
    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

    @Override
    public List<DimensionIdentifier> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }

    @Override
    public Map<DimensionIdentifier, Iterable<? extends Serializable>> getSelection() {
        return selectionMappedByDimension;
    }

    public void appendDimensionToGroupBy(DimensionIdentifier dimension) {
        dimensionsToGroupBy.add(dimension);
    }

    public void setSelectionFor(DimensionIdentifier dimension, Iterable<? extends Serializable> selection) {
        selectionMappedByDimension.put(dimension, selection);
    }

}
