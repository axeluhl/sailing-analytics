package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.gwt.ui.client.shared.components.Component;

public interface StatisticProvider extends Component<Object> {
    
    public StatisticType getStatisticType();
    public AggregatorType getAggregatorType();
    public DataTypes getDataType();

    public void addStatisticChangedListener(StatisticChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinition queryDefinition);

}
