package com.sap.sse.datamining.ui.client;

import java.util.Collection;

import com.google.gwt.user.client.ui.ValueListBox;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface GroupingProvider
        extends DataMiningComponentProvider<SerializableSettings>, DataRetrieverChainDefinitionChangedListener {
    Collection<FunctionDTO> getDimensionsToGroupBy();

    void addGroupingChangedListener(GroupingChangedListener listener);

    void removeDimensionToGroupBy(FunctionDTO dimension);

    ValueListBox<FunctionDTO> createDimensionToGroupByBoxWithoutEventHandler();

    Iterable<FunctionDTO> getAvailableDimensions();

    /**
     * Sets the i-th dimension to group by to the function specified by {@code dimensionToGroupBy}. If {@code i} matches
     * the last grouping dimension drop-down, its value is set and another dimension drop-down is created if there are
     * dimensions left to select for grouping. Otherwise, the selected drop-down's value is set to the function
     * identified by {@code dimensionToGroupBy}.
     */
    void setDimensionToGroupBy(int i, FunctionDTO dimensionToGroupBy);

}
