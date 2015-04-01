package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class SingleRetrieverLevelSelectionProvider implements Component<Object> {

    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    
    private final DataMiningSession session;
    private final DataRetrieverChainDefinitionDTO retrieverChain;
    private final LocalizedTypeDTO retrievedDataType;
    private final int retrieverLevel;
    private final List<FunctionDTO> availableDimensions;
    
    private final HorizontalPanel mainPanel;
    private final Collection<SingleDimensionFilterSelectionProvider> dimensionFilters;
    private final SelectionChangeEvent.Handler selectionTablesChangedListener;

    public SingleRetrieverLevelSelectionProvider(DataMiningSession session, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionDTO retrieverChain, LocalizedTypeDTO retrievedDataType, int retrieverLevel) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        
        this.session = session;
        this.retrieverChain = retrieverChain;
        this.retrievedDataType = retrievedDataType;
        this.retrieverLevel = retrieverLevel;
        availableDimensions = new ArrayList<>();
        
        mainPanel = new HorizontalPanel();
        mainPanel.setSpacing(5);
        dimensionFilters = new ArrayList<>();
        selectionTablesChangedListener = new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                notifyListeners();
            }
        };

        Label label = new Label(retrievedDataType.getDisplayName() + " " + this.stringMessages.filterBy());
        mainPanel.add(label);
    }

    public void setAvailableDimensions(Collection<FunctionDTO> dimensions) {
        availableDimensions.clear();
        availableDimensions.addAll(dimensions);
        Collections.sort(availableDimensions);
        
        SingleDimensionFilterSelectionProvider dimensionFilter = createDimensionFilterSelectionProvider();
        dimensionFilter.setAvailableDimensions(availableDimensions);
        addDimensionFilter(dimensionFilter);
    }

    private SingleDimensionFilterSelectionProvider createDimensionFilterSelectionProvider() {
        SingleDimensionFilterSelectionProvider dimensionFilter = new SingleDimensionFilterSelectionProvider(dataMiningService, errorReporter, session,
                                                                                                            this);
        dimensionFilter.addSelectionChangeHandler(selectionTablesChangedListener);
        return dimensionFilter;
    }
    
    private void addDimensionFilter(SingleDimensionFilterSelectionProvider dimensionFilter) {
        dimensionFilters.add(dimensionFilter);
        mainPanel.add(dimensionFilter.getEntryWidget());
    }

    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        HashMap<FunctionDTO, Collection<? extends Serializable>> filterSelection = new HashMap<>();
        for (SingleDimensionFilterSelectionProvider dimensionFilter : dimensionFilters) {
            Collection<? extends Serializable> dimensionFilterSelection = dimensionFilter.getSelection();
            if (!dimensionFilterSelection.isEmpty()) {
                filterSelection.put(dimensionFilter.getSelectedDimension(), dimensionFilterSelection);
            }
        }
        return filterSelection;
    }

    public void applySelection(Map<FunctionDTO, Collection<? extends Serializable>> filterSelection) {
        //TODO
    }

    public void clearSelection() {
        for (SingleDimensionFilterSelectionProvider dimensionFilter : dimensionFilters) {
            dimensionFilter.clearSelection();
        }
    }
    
    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (SelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    DataRetrieverChainDefinitionDTO getDataRetrieverChain() {
        return retrieverChain;
    }
    
    public int getRetrieverLevel() {
        return retrieverLevel;
    }
    
    public LocalizedTypeDTO getRetrievedDataType() {
        return retrievedDataType;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Object> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Object newSettings) {
    }

    @Override
    public String getLocalizedShortName() {
        return getClass().getSimpleName();
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public boolean isVisible() {
        return mainPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        mainPanel.setVisible(visibility);
    }

    @Override
    public String getDependentCssClassName() {
        return "singleRetrieverLevelSelectionPanel";
    }

}
