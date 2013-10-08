package com.sap.sailing.datamining.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gwt.dev.util.collect.HashMap;
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

    public GrouperType getGrouperType() {
        return grouperType;
    }

    public StatisticType getStatisticType() {
        return statisticType;
    }

    public AggregatorType getAggregatorType() {
        return aggregatorType;
    }

    public String getCustomGrouperScriptText() {
        return customGrouperScriptText;
    }

    public List<DimensionType> getDimensionsToGroupBy() {
        return dimensionsToGroupBy;
    }

    public Map<DimensionType, Iterable<?>> getSelection() {
        return selectionMappedByDimension;
    }

    protected void appendDimensionToGroupBy(DimensionType dimension) {
        dimensionsToGroupBy.add(dimension);
    }

    protected void setSelectionFor(DimensionType dimension, Collection<?> selection) {
        selectionMappedByDimension.put(dimension, selection);
    }

    protected void setCustomGrouperScriptText(String scriptText) {
        customGrouperScriptText = scriptText;
    }

}
