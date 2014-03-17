package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.datamining.selection.SimpleStatistic;
import com.sap.sse.datamining.shared.components.AggregatorType;

public interface StatisticProvider extends Component<Object> {
    
    public StatisticType getStatisticType();
    public AggregatorType getAggregatorType();
    public DataTypes getDataType();
    public SimpleStatistic getStatistic();

    public void addStatisticChangedListener(StatisticChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinition queryDefinition);

}
