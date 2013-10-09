package com.sap.sailing.datamining.impl;

import java.util.List;
import java.util.Map;

import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.datamining.shared.StatisticType;

public class SmartQueryDefinition implements QueryDefinition {
    private static final long serialVersionUID = -4195720362233906108L;
    
    private QueryDefinition queryDefinition;

    public SmartQueryDefinition(QueryDefinition queryDefinition) {
        this.queryDefinition = queryDefinition;
    }

    @Override
    public GrouperType getGrouperType() {
        return queryDefinition.getGrouperType();
    }

    @Override
    public StatisticType getStatisticType() {
        return queryDefinition.getStatisticType();
    }

    @Override
    public AggregatorType getAggregatorType() {
        return queryDefinition.getAggregatorType();
    }

    @Override
    public String getCustomGrouperScriptText() {
        return queryDefinition.getCustomGrouperScriptText();
    }

    @Override
    public List<SharedDimensions> getDimensionsToGroupBy() {
        return queryDefinition.getDimensionsToGroupBy();
    }

    @Override
    public Map<SharedDimensions, Iterable<?>> getSelection() {
        return queryDefinition.getSelection();
    }

    public DataTypes getDataType() {
        //TODO Data type should depend on the statistic to calculate
        return DataTypes.GPSFix;
    }

}
