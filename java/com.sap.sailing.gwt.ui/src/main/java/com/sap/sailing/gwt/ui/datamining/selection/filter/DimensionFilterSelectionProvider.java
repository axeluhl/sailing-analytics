package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

class DimensionFilterSelectionProvider {
    
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final RetrieverLevelFilterSelectionProvider retrieverLevelSelectionProvider;

    private final FlowPanel mainPanel;
    
    private final HorizontalPanel controlsPanel;
    private final ValueListBox<FunctionDTO> dimensionListBox;
    private final DimensionChangedHandler dimensionChangedHandler;
    private final ToggleButton toggleFilterButton;
    
    private final SimpleBusyIndicator busyIndicator;
    private final FilterableSelectionTable<?> selectionTable;
    private Collection<?> selectionToBeApplied;
    
    public DimensionFilterSelectionProvider(DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter, DataMiningSession session,
                                            RetrieverLevelFilterSelectionProvider retrieverLevelSelectionProvider) {
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.session = session;
        this.retrieverLevelSelectionProvider = retrieverLevelSelectionProvider;
        
        mainPanel = new FlowPanel();
        
        controlsPanel = new HorizontalPanel();
        controlsPanel.setSpacing(2);
        mainPanel.add(controlsPanel);
        
        dimensionListBox = new ValueListBox<FunctionDTO>(new AbstractObjectRenderer<FunctionDTO>() {
            @Override
            protected String convertObjectToString(FunctionDTO function) {
                return function.getDisplayName();
            }
        });
        dimensionChangedHandler = new DimensionChangedHandler();
        dimensionListBox.addValueChangeHandler(dimensionChangedHandler);
        controlsPanel.add(dimensionListBox);
        
        toggleFilterButton = new ToggleButton("S");
        toggleFilterButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionTable.setFilteringEnabled(toggleFilterButton.isDown());
            }
        });
        toggleFilterButton.setVisible(false);
        controlsPanel.add(toggleFilterButton);
        
        busyIndicator = new SimpleBusyIndicator(true, 0.7f);
        busyIndicator.setVisible(false);
        mainPanel.add(busyIndicator);
        
        selectionTable = new FilterableSelectionTable<>();
        selectionTable.addSelectionChangeHandler(new FilterSelectionChangedListener() {
            @Override
            public void selectionChanged() {
                DimensionFilterSelectionProvider.this.retrieverLevelSelectionProvider.dimensionFilterSelectionChanged(DimensionFilterSelectionProvider.this);
            }
        });
        selectionTable.setVisible(false);
        mainPanel.add(selectionTable);
    }
    
    private class DimensionChangedHandler implements ValueChangeHandler<FunctionDTO> {
        boolean firstChange = true;
        
        @Override
        public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
            final FunctionDTO dimension = event.getValue();
            if (dimension != null) {
                fetchAndSetAvailableData();
                if (firstChange && retrieverLevelSelectionProvider.canAddDimensionSelectionProvider()) {
                    retrieverLevelSelectionProvider.createAndAddDimensionSelectionProvider();
                }
                firstChange = false;
            } else if (retrieverLevelSelectionProvider.shouldRemoveDimensionSelectionProvider()) {
                retrieverLevelSelectionProvider.removeDimensionFilter(DimensionFilterSelectionProvider.this);
                firstChange = false;
            } else {
                selectionTable.clearSelection();
                selectionTable.setContent(new ArrayList<>(), true);
                
                selectionTable.setVisible(false);
                toggleFilterButton.setVisible(false);
                firstChange = true;
            }
            retrieverLevelSelectionProvider.updateAvailableDimensions();
        }
    }
    
    private void fetchAndSetAvailableData() {
        fetchAndDisplayAvailableData(false, true, null);
    }
    
    void fetchAndUpdateAvailableData(final Iterator<DimensionFilterSelectionProvider> retrieverLevelSelectionProviderIterator) {
        fetchAndDisplayAvailableData(true, false, retrieverLevelSelectionProviderIterator);
    }
    
    /**
     * Fetches the dimension values from the server and displays it. The data is filtered by the current filter
     * selection (of all retriever levels).
     * 
     * @param isUpdate <code>true</code>, if the call should update the data and preserve the selection and 
     *                 <code>false</code>, if it should override the data and clear the selection.
     * @param selectionChangedCallback An {@link AsyncCallback} to react, if the selection has been changed or not.
     */
    private void fetchAndDisplayAvailableData(final boolean isUpdate, final boolean notifyListenersWhenSelectionChanged,
            final Iterator<DimensionFilterSelectionProvider> retrieverLevelSelectionProviderIterator) {
        final FunctionDTO dimension = getSelectedDimension();
        Collection<FunctionDTO> dimensionDTOs = new ArrayList<>();
        dimensionDTOs.add(dimension);
        @SuppressWarnings("unchecked")
        Map<Integer, Map<FunctionDTO, Collection<?>>> filterSelectionDTO = 
                (Map<Integer, Map<FunctionDTO, Collection<?>>>)(Map<?, ?>) retrieverLevelSelectionProvider.getCompleteFilterSelection();
        int retrieverLevel = retrieverLevelSelectionProvider.getRetrieverLevel();
        if (filterSelectionDTO.containsKey(retrieverLevel)) {
            filterSelectionDTO.get(retrieverLevel).remove(dimension);
        }
        busyIndicator.setVisible(true);
        dataMiningService.getDimensionValuesFor(session, retrieverLevelSelectionProvider.getDataRetrieverChain(),
                retrieverLevelSelectionProvider.getRetrieverLevel(), dimensionDTOs, filterSelectionDTO,
                LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<QueryResult<Set<Object>>>() {
                    @Override
                    public void onSuccess(QueryResult<Set<Object>> result) {
                        if (!result.getResults().isEmpty()) {
                            GroupKey contentKey = new GenericGroupKey<FunctionDTO>(dimension);
                            List<?> content = new ArrayList<Object>(result.getResults().get(contentKey));
                            Collections.sort(content, new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    return o1.toString().compareTo(o2.toString());
                                }
                            });
                            
                            boolean selectionChanged;
                            if (isUpdate) {
                                selectionChanged = selectionTable.updateContent(content, notifyListenersWhenSelectionChanged);
                            } else {
                                selectionChanged = selectionTable.setContent(content, notifyListenersWhenSelectionChanged);
                            }
                            
                            if (selectionToBeApplied != null) {
                                selectionTable.setSelection(selectionToBeApplied, notifyListenersWhenSelectionChanged);
                                selectionToBeApplied = null;
                            }
                            busyIndicator.setVisible(false);
                            selectionTable.setVisible(true);
                            toggleFilterButton.setVisible(true);
                            
                            if (retrieverLevelSelectionProviderIterator != null) {
                                if (selectionChanged) {
                                    //Update the complete retriever level, because the selection changed
                                    retrieverLevelSelectionProvider.updateAvailableData(getSelectedDimension());
                                } else {
                                    //Continue with the update of the retriever level selection provider
                                    retrieverLevelSelectionProvider.updateAvailableData(getSelectedDimension(), retrieverLevelSelectionProviderIterator);
                                }
                            }
                        }
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the dimension values of " + dimension + ": "
                                + caught.getMessage());
                    }
                });
    }
    
    void setAvailableDimensions(Collection<FunctionDTO> availableDimensions) {
        dimensionListBox.setAcceptableValues(availableDimensions);
    }

    public FunctionDTO getSelectedDimension() {
        return dimensionListBox.getValue();
    }

    public Collection<? extends Serializable> getSelection() {
        return selectionTable.getSelection();
    }
    
    void clearSelection() {
        selectionTable.clearSelection();
    }

    void setSelectedDimensionAndValues(FunctionDTO functionDTO, Collection<?> selection) {
        dimensionChangedHandler.firstChange = false;
        selectionToBeApplied = selection;
        dimensionListBox.setValue(functionDTO, true);
    }
    
    public Widget getEntryWidget() {
        return mainPanel;
    }

    public void resizeToHeight(int heightInPX) {
        int widthInPX = controlsPanel.getOffsetWidth();
        mainPanel.setSize(widthInPX + "px", heightInPX + "px");
        
        int remainingHeightInPX = Math.max(0, heightInPX - controlsPanel.getOffsetHeight());
        selectionTable.resizeTo(widthInPX, remainingHeightInPX);
    }
    
}