package com.sap.sailing.datamining.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.i18n.client.LocaleInfo;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.Components.StatisticType;

public class SimpleQueryDefinition implements QueryDefinition {
    private static final long serialVersionUID = 3476324726640558091L;

    private String localeName;
    
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
    SimpleQueryDefinition() { }

    public SimpleQueryDefinition(LocaleInfo localeInfo, GrouperType grouperType, StatisticType statisticType, AggregatorType aggregatorType, DataTypes dataType) {
        this.localeName = localeInfo.getLocaleName();
        this.grouperType = grouperType;
        this.statisticType = statisticType;
        this.aggregatorType = aggregatorType;
        this.dataType = dataType;

        dimensionsToGroupBy = new ArrayList<DimensionIdentifier>();
        selectionMappedByDimension = new HashMap<DimensionIdentifier, Iterable<?>>();
    }
    
    @Override
    public String getLocaleName() {
        return localeName;
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
