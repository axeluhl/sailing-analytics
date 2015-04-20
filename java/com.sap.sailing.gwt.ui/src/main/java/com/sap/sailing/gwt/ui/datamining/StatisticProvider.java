package com.sap.sailing.gwt.ui.datamining;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface StatisticProvider extends Component<AbstractSettings> {
    
    public FunctionDTO getStatisticToCalculate();

    public AggregatorType getAggregatorType();

    public void addStatisticChangedListener(StatisticChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

}
