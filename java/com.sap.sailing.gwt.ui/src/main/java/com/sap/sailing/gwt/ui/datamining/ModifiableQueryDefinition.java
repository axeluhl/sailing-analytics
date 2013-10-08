package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.SimpleQueryDefinition;
import com.sap.sailing.datamining.shared.StatisticType;

public class ModifiableQueryDefinition<DimensionType> extends SimpleQueryDefinition<DimensionType> {
    private static final long serialVersionUID = 3476324726640558091L;

    public ModifiableQueryDefinition(GrouperType grouperType, StatisticType statisticType, AggregatorType aggregatorType) {
        super(grouperType, statisticType, aggregatorType);
    }

    public void appendDimensionToGroupBy(DimensionType dimension) {
        super.appendDimensionToGroupBy(dimension);
    }

    public void setSelectionFor(DimensionType dimension, Collection<?> selection) {
        super.setSelectionFor(dimension, selection);
    }

    public void setCustomGrouperScriptText(String scriptText) {
        super.setCustomGrouperScriptText(scriptText);
    }

}
