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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
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
import com.sap.sse.gwt.client.panels.HorizontalFlowPanel;

public class SingleRetrieverLevelSelectionProvider implements Component<Object> {

    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    
    private final DataMiningSession session;
    private final LocalizedTypeDTO retrievedDataType;
    private final int retrieverLevel;
    private final List<FunctionDTO> availableDimensions;
    
    private final HorizontalFlowPanel mainPanel;
    private final Collection<SingleDimensionFilterSelectionProvider> dimensionFilters;

    public SingleRetrieverLevelSelectionProvider(DataMiningSession session, StringMessages stringMessages,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
            DataRetrieverChainDefinitionDTO retrieverChain, LocalizedTypeDTO retrievedDataType, int retrieverLevel) {
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        
        this.session = session;
        this.retrievedDataType = retrievedDataType;
        this.retrieverLevel = retrieverLevel;
        availableDimensions = new ArrayList<>();
        
        dimensionFilters = new ArrayList<>();
        mainPanel = new HorizontalFlowPanel();
        mainPanel.setWidth("100%");

        addWidgetWithMargin(new Label(retrievedDataType.getDisplayName() + " " + stringMessages.filterBy()));
    }

    public void setAvailableDimensions(Collection<FunctionDTO> dimensions) {
        availableDimensions.clear();
        availableDimensions.addAll(dimensions);
        Collections.sort(availableDimensions);
        
        SingleDimensionFilterSelectionProvider dimensionFilter = new SingleDimensionFilterSelectionProvider(stringMessages, dataMiningService, errorReporter,
                                                                                                            session, this);
        addDimensionFilter(dimensionFilter);
    }
    
    private void addDimensionFilter(SingleDimensionFilterSelectionProvider dimensionFilter) {
        dimensionFilters.add(dimensionFilter);
        addWidgetWithMargin(dimensionFilter.getEntryWidget());
    }

    private void addWidgetWithMargin(Widget widget) {
        widget.getElement().getStyle().setMargin(2, Unit.PX);
        mainPanel.add(widget);
    }

    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        HashMap<FunctionDTO, Collection<? extends Serializable>> filterSelection = new HashMap<>();
        for (SingleDimensionFilterSelectionProvider dimensionFilter : dimensionFilters) {
            Collection<? extends Serializable> dimensionFilterSelection = dimensionFilter.getSelection();
            if (!dimensionFilterSelection.isEmpty()) {
                filterSelection.put(dimensionFilter.getDimension(), dimensionFilterSelection);
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

//    private void notifyListeners() {
//        for (SelectionChangedListener listener : listeners) {
//            listener.selectionChanged();
//        }
//    }
    
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
