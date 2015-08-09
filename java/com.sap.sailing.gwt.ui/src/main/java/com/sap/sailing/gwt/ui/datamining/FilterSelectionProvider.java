package com.sap.sailing.gwt.ui.datamining;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface FilterSelectionProvider extends DataMiningComponentProvider, DataRetrieverChainDefinitionChangedListener {

    public void addSelectionChangedListener(FilterSelectionChangedListener listener);

    public Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> getSelection();

    public void applySelection(StatisticQueryDefinitionDTO queryDefinition);

    public void clearSelection();

}