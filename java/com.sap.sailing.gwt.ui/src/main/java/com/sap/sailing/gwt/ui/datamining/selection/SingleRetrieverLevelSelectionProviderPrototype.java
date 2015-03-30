package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

public class SingleRetrieverLevelSelectionProviderPrototype extends HorizontalPanel {
    
    /* This implementation is a prototype and mustn't be used to develop a productive UI component.
     * Instead start from scratch and use this prototype as orientation to get a cleaner result.
     * */

    private final DataMiningSession session;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final Set<SelectionChangedListener> listeners;
    
    private final DataRetrieverChainDefinitionDTO retrieverChain;
    private final LocalizedTypeDTO retrievedDataType;
    private final int retrieverLevel;
    private final List<FunctionDTO> availableDimensions;
    private final Map<GroupKey, Set<Object>> dimensionValuesMappedByDimension;
    private final SelectionChangeEvent.Handler selectionTablesChangedListener;
    
    private Map<FunctionDTO, Collection<? extends Serializable>> filterSelectionToBeApplied;
    
    private final Label filterByLabel;
    private final HorizontalPanel labeledBusyIndicator;
    private final HorizontalPanel filterSelectionPanel;
    
    private final List<ValueListBox<FunctionDTO>> dimensionToFilterByBoxes;
    private final Map<ValueListBox<?>, VerticalPanel> singleDimensionFilterSelectionPanelsMappedBySelectionBox;
    private final Map<ValueListBox<?>, SelectionTable<?>> selectionTablesMappedBySelectionBox;

    public SingleRetrieverLevelSelectionProviderPrototype(DataMiningSession session, StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                                 DataRetrieverChainDefinitionDTO retrieverChain, LocalizedTypeDTO retrievedDataType, int retrieverLevel) {
        this.session = session;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        listeners = new HashSet<>();
        
        this.retrieverChain = retrieverChain;
        this.retrievedDataType = retrievedDataType;
        this.retrieverLevel = retrieverLevel;
        availableDimensions = new ArrayList<>();
        dimensionValuesMappedByDimension = new HashMap<>();
        selectionTablesChangedListener = new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                notifyListeners();
            }
        };
        
        this.setSpacing(5);
        
        add(new Label(retrievedDataType.getDisplayName()));
        filterByLabel = new Label(stringMessages.filterBy());
        filterByLabel.setVisible(false);
        add(filterByLabel);
        
        labeledBusyIndicator = new HorizontalPanel();
        labeledBusyIndicator.setVisible(false);
        labeledBusyIndicator.setSpacing(5);
        BusyIndicator busyIndicator = new SimpleBusyIndicator(true, 0.7f);
        labeledBusyIndicator.add(busyIndicator);
        labeledBusyIndicator.add(new Label(stringMessages.loadingDimensionValues()));
        add(labeledBusyIndicator);
        
        filterSelectionPanel = new HorizontalPanel();
        filterSelectionPanel.setVisible(false);
        add(filterSelectionPanel);
        
        dimensionToFilterByBoxes = new ArrayList<>();
        singleDimensionFilterSelectionPanelsMappedBySelectionBox = new HashMap<>();
        selectionTablesMappedBySelectionBox = new HashMap<>();
    }
    
    public LocalizedTypeDTO getRetrievedDataType() {
        return retrievedDataType;
    }
    
    public int getRetrieverLevel() {
        return retrieverLevel;
    }

    public void setAvailableDimensions(Collection<FunctionDTO> dimensions) {
        clearFilterSelectionComponents();

        availableDimensions.clear();
        availableDimensions.addAll(dimensions);
        
        boolean isFiltrationPossible = !availableDimensions.isEmpty();
        filterByLabel.setVisible(isFiltrationPossible);
        filterSelectionPanel.setVisible(isFiltrationPossible);
        if (isFiltrationPossible) {
            Collections.sort(availableDimensions);
            labeledBusyIndicator.setVisible(true);
            dataMiningService.getDimensionValuesFor(session, retrieverChain, retrieverLevel, dimensions, LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<QueryResult<Set<Object>>>() {
                @Override
                public void onSuccess(QueryResult<Set<Object>> result) {
                    dimensionValuesMappedByDimension.clear();
                    dimensionValuesMappedByDimension.putAll(result.getResults());
                    
                    ValueListBox<FunctionDTO> firstDimensionToFilterByBox = createDimensionToGroupByBox();
                    addDimensionToFilterByBox(firstDimensionToFilterByBox);
                    updateAcceptableValues();
                    
                    if (filterSelectionToBeApplied != null) {
                        applySelection(filterSelectionToBeApplied);
                        filterSelectionToBeApplied = null;
                    }

                    labeledBusyIndicator.setVisible(false);
                }
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error fetching the dimension values: " + caught.getMessage());
                    labeledBusyIndicator.setVisible(false);
                }
            });
        }
    }

    private ValueListBox<FunctionDTO> createDimensionToGroupByBox() {
        ValueListBox<FunctionDTO> dimensionToGroupByBox = new ValueListBox<FunctionDTO>(new AbstractObjectRenderer<FunctionDTO>() {
            @Override
            protected String convertObjectToString(FunctionDTO function) {
                return function.getDisplayName();
            }
            
        });
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<FunctionDTO>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
                ValueListBox<?> changedDimensionToFilterByBox = (ValueListBox<?>) event.getSource();
                FunctionDTO selectedDimension = event.getValue();
                if (firstChange && selectedDimension != null) {
                    ValueListBox<FunctionDTO> newDimensionToFilterByBox = createDimensionToGroupByBox();
                    addDimensionToFilterByBox(newDimensionToFilterByBox);
                }
                boolean selectionWasNotEmpty = selectionTablesMappedBySelectionBox.containsKey(changedDimensionToFilterByBox) &&
                                               !selectionTablesMappedBySelectionBox.get(changedDimensionToFilterByBox).getSelectionAsValues().isEmpty();
                if (selectedDimension != null) { 
                    SelectionTable<?> selectionTable = new SelectionTable<>(selectedDimension, false);
                    selectionTable.addSelectionChangeHandler(selectionTablesChangedListener);
                    setSelectionTableFor(changedDimensionToFilterByBox, selectionTable);
                    selectionTable.updateContent(getDimensionValuesFor(selectedDimension));
                    if (!firstChange && selectionWasNotEmpty) {
                        notifyListeners();
                    }
                } else if (selectedDimension == null) {
                    removeDimensionToFilterByBox(changedDimensionToFilterByBox);
                    if (selectionWasNotEmpty) {
                        notifyListeners();
                    }
                }
                
                updateAcceptableValues();
                firstChange = false;
            }
        });
        return dimensionToGroupByBox;
    }

    private void addDimensionToFilterByBox(ValueListBox<FunctionDTO> dimensionToFilterByBox) {
        dimensionToFilterByBoxes.add(dimensionToFilterByBox);
        
        VerticalPanel singleDimensionFilterSelectionPanel = new VerticalPanel();
        singleDimensionFilterSelectionPanel.add(dimensionToFilterByBox);
        singleDimensionFilterSelectionPanelsMappedBySelectionBox.put(dimensionToFilterByBox, singleDimensionFilterSelectionPanel);
        
        filterSelectionPanel.add(singleDimensionFilterSelectionPanel);
    }

    private void setSelectionTableFor(ValueListBox<?> changedDimensionToFilterByBox, SelectionTable<?> selectionTable) {
        if (selectionTablesMappedBySelectionBox.containsKey(changedDimensionToFilterByBox)) {
            SelectionTable<?> oldSelectionTable = selectionTablesMappedBySelectionBox.get(changedDimensionToFilterByBox);
            singleDimensionFilterSelectionPanelsMappedBySelectionBox.get(changedDimensionToFilterByBox).remove(oldSelectionTable);
            
            selectionTablesMappedBySelectionBox.remove(changedDimensionToFilterByBox);
        }
        
        VerticalPanel singleDimensionFilteringSelectionPanel = singleDimensionFilterSelectionPanelsMappedBySelectionBox.get(changedDimensionToFilterByBox);
        singleDimensionFilteringSelectionPanel.add(selectionTable);
        selectionTable.setWidth(singleDimensionFilteringSelectionPanel.getOffsetWidth() + "px");
        selectionTable.setHeight("200px");
        selectionTablesMappedBySelectionBox.put(changedDimensionToFilterByBox, selectionTable);
    }

    private Collection<?> getDimensionValuesFor(FunctionDTO selectedDimension) {
        GroupKey selectedDimensionAsGroupKey = new GenericGroupKey<>(selectedDimension);
        return dimensionValuesMappedByDimension.containsKey(selectedDimensionAsGroupKey) ? dimensionValuesMappedByDimension.get(selectedDimensionAsGroupKey) : new ArrayList<>();
    }

    private void removeDimensionToFilterByBox(ValueListBox<?> dimensionToFilterByBox) {
        dimensionToFilterByBoxes.remove(dimensionToFilterByBox);
        
        VerticalPanel singleDimensionFilterSelectionPanel = singleDimensionFilterSelectionPanelsMappedBySelectionBox.remove(dimensionToFilterByBox);
        filterSelectionPanel.remove(singleDimensionFilterSelectionPanel);
        
        selectionTablesMappedBySelectionBox.remove(dimensionToFilterByBox);
    }

    private void updateAcceptableValues() {
        for (ValueListBox<FunctionDTO> dimensionToGroupByBox : dimensionToFilterByBoxes) {
            List<FunctionDTO> acceptableValues = new ArrayList<FunctionDTO>(availableDimensions);
            acceptableValues.removeAll(getDimensionsToFilterBy());
            if (dimensionToGroupByBox.getValue() != null) {
                acceptableValues.add(dimensionToGroupByBox.getValue());
            }
            Collections.sort(acceptableValues);
            acceptableValues.add(null);
            dimensionToGroupByBox.setAcceptableValues(acceptableValues);
        }
    }

    private Collection<FunctionDTO> getDimensionsToFilterBy() {
        Collection<FunctionDTO> dimensionsToFilterBy = new ArrayList<FunctionDTO>();
        for (ValueListBox<FunctionDTO> dimensionListBox : dimensionToFilterByBoxes) {
            if (dimensionListBox.getValue() != null) {
                dimensionsToFilterBy.add(dimensionListBox.getValue());
            }
        }
        return dimensionsToFilterBy;
    }

    private void clearFilterSelectionComponents() {
        singleDimensionFilterSelectionPanelsMappedBySelectionBox.clear();
        filterSelectionPanel.clear();
        dimensionToFilterByBoxes.clear();
        selectionTablesMappedBySelectionBox.clear();
    }

    public Map<FunctionDTO, Collection<? extends Serializable>> getFilterSelection() {
        HashMap<FunctionDTO, Collection<? extends Serializable>> filterSelection = new HashMap<>();
        for (SelectionTable<?> selectionTable : selectionTablesMappedBySelectionBox.values()) {
            Collection<? extends Serializable> tableSelection = selectionTable.getSelectionAsValues();
            if (!tableSelection.isEmpty()) {
                filterSelection.put(selectionTable.getDimension(), tableSelection);
            }
        }
        return filterSelection;
    }

    public void applySelection(Map<FunctionDTO, Collection<? extends Serializable>> filterSelection) {
        if (!isDataAvailable()) {
            filterSelectionToBeApplied = filterSelection;
        } else {
            clearFilterSelectionComponents();

            ValueListBox<FunctionDTO> firstDimensionToFilterByBox = createDimensionToGroupByBox();
            addDimensionToFilterByBox(firstDimensionToFilterByBox);
            updateAcceptableValues();
            
            int filterBoxIndex = 0;
            for (Entry<FunctionDTO, Collection<? extends Serializable>> singleDimensionFilterSelection : filterSelection.entrySet()) {
                ValueListBox<FunctionDTO> dimensionToFilterByBox = dimensionToFilterByBoxes.get(filterBoxIndex);
                dimensionToFilterByBox.setValue(singleDimensionFilterSelection.getKey(), true);
                selectionTablesMappedBySelectionBox.get(dimensionToFilterByBox).setSelection(singleDimensionFilterSelection.getValue());
            }
        }
    }
    
    private boolean isDataAvailable() {
        return !dimensionValuesMappedByDimension.isEmpty();
    }

    public void clearSelection() {
        for (SelectionTable<?> selectionTable : selectionTablesMappedBySelectionBox.values()) {
            selectionTable.clearSelection();
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

}
