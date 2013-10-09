package com.sap.sailing.datamining.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.Components.GrouperType;

public class SimpleQueryDefinition<DimensionType> implements QueryDefinition<DimensionType> {
    private static final long serialVersionUID = 3476324726640558091L;
    
    private GrouperType grouperType;
    private StatisticType statisticType;
    private AggregatorType aggregatorType;

    private String customGrouperScriptText;
    private List<DimensionType> dimensionsToGroupBy;
    private Map<DimensionType, Iterable<?>> selectionMappedByDimension;
    
    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    SimpleQueryDefinition() { }

    public SimpleQueryDefinition(GrouperType grouperType, StatisticType statisticType, AggregatorType aggregatorType) {
        this.grouperType = grouperType;
        this.statisticType = statisticType;
        this.aggregatorType = aggregatorType;

        customGrouperScriptText = "";
        dimensionsToGroupBy = new ArrayList<DimensionType>();
        selectionMappedByDimension = new HashMap<DimensionType, Iterable<?>>();
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
    public String getCustomGrouperScriptText() {
        return customGrouperScriptText;
    }

    @Override
    public List<DimensionType> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }

    @Override
    public Map<DimensionType, Iterable<?>> getSelection() {
        return selectionMappedByDimension;
    }

    public void appendDimensionToGroupBy(DimensionType dimension) {
        dimensionsToGroupBy.add(dimension);
    }

    public void setSelectionFor(DimensionType dimension, Collection<?> selection) {
        selectionMappedByDimension.put(dimension, selection);
    }

    public void setCustomGrouperScriptText(String scriptText) {
        customGrouperScriptText = scriptText;
    }

}
