package com.sap.sailing.datamining.shared.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.components.GrouperType;

public class QueryDefinitionDeprecatedImpl implements QueryDefinitionDeprecated {
    private static final long serialVersionUID = 8408302866342877839L;

    private String localeInfoName;
    
    private GrouperType grouperType;
    private StatisticType statisticType;
    private AggregatorType aggregatorType;
    private DataTypes dataType;

    private List<DimensionIdentifier> dimensionsToGroupBy;
    private Map<DimensionIdentifier, Iterable<?>> selectionMappedByDimension;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    QueryDefinitionDeprecatedImpl() { }

    public QueryDefinitionDeprecatedImpl(String localeInfoName, GrouperType grouperType, StatisticType statisticType, AggregatorType aggregatorType, DataTypes dataType) {
        this.localeInfoName = localeInfoName;
        this.grouperType = grouperType;
        this.statisticType = statisticType;
        this.aggregatorType = aggregatorType;
        this.dataType = dataType;

        dimensionsToGroupBy = new ArrayList<DimensionIdentifier>();
        selectionMappedByDimension = new HashMap<DimensionIdentifier, Iterable<?>>();
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
    public StatisticType getStatisticType() {
        return statisticType;
    }

    @Override
    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }
    
    @Override
    public DataTypes getDataType() {
        return dataType;
    }

    @Override
    public List<DimensionIdentifier> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }

    @Override
    public Map<DimensionIdentifier, Iterable<?>> getSelection() {
        return selectionMappedByDimension;
    }

    public void appendDimensionToGroupBy(DimensionIdentifier dimension) {
        dimensionsToGroupBy.add(dimension);
    }

    public void setSelectionFor(DimensionIdentifier dimension, Collection<?> selection) {
        selectionMappedByDimension.put(dimension, selection);
    }

}
