package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface GroupingProvider
        extends DataMiningComponentProvider<SerializableSettings>, DataRetrieverChainDefinitionChangedListener {
    Collection<FunctionDTO> getDimensionsToGroupBy();

    String getCustomGrouperScriptText();

    void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition);

    void addGroupingChangedListener(GroupingChangedListener listener);

    void removeDimensionToGroupBy(FunctionDTO dimension);

}
