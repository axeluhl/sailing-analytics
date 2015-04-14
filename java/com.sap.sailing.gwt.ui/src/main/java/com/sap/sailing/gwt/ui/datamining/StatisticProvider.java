package com.sap.sailing.gwt.ui.datamining;

import com.sap.sailing.domain.common.settings.Settings;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sse.datamining.shared.QueryDefinitionDTO;
import com.sap.sse.datamining.shared.components.AggregatorType;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface StatisticProvider extends Component<Settings> {
    
    public FunctionDTO getStatisticToCalculate();

    public AggregatorType getAggregatorType();

    public void addStatisticChangedListener(StatisticChangedListener listener);
    
    public void applyQueryDefinition(QueryDefinitionDTO queryDefinition);

}
