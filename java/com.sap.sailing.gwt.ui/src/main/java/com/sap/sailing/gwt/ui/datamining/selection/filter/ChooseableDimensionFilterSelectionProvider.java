package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.ManagedDataMiningQueriesCounter;
import com.sap.sailing.gwt.ui.datamining.execution.ManagedDataMiningQueryCallback;
import com.sap.sailing.gwt.ui.datamining.execution.SimpleManagedDataMiningQueriesCounter;
import com.sap.sailing.gwt.ui.datamining.resources.DataMiningResources;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

class ChooseableDimensionFilterSelectionProvider {

    private static final DataMiningResources resources = GWT.create(DataMiningResources.class);
    
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final RetrieverLevelFilterSelectionProvider retrieverLevelSelectionProvider;
    private final ManagedDataMiningQueriesCounter counter;

    private final FlowPanel mainPanel;
    
    private final HorizontalPanel controlsPanel;
    private final ValueListBox<FunctionDTO> dimensionListBox;
    private final DimensionChangedHandler dimensionChangedHandler;
    private final ToggleButton toggleFilterButton;
    
    private final SimpleBusyIndicator busyIndicator;
    private final FilterableSelectionTable<?> selectionTable;
    private Collection<?> selectionToBeApplied;
    
    public ChooseableDimensionFilterSelectionProvider(DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter, DataMiningSession session,
                                            RetrieverLevelFilterSelectionProvider retrieverLevelSelectionProvider) {
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.session = session;
        this.retrieverLevelSelectionProvider = retrieverLevelSelectionProvider;
        counter = new SimpleManagedDataMiningQueriesCounter();
        
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
        dimensionListBox.setWidth("100%");
        dimensionChangedHandler = new DimensionChangedHandler();
        dimensionListBox.addValueChangeHandler(dimensionChangedHandler);
        controlsPanel.add(dimensionListBox);

        toggleFilterButton = new ToggleButton(new Image(resources.searchIcon()));
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
                ChooseableDimensionFilterSelectionProvider.this.retrieverLevelSelectionProvider.dimensionFilterSelectionChanged(ChooseableDimensionFilterSelectionProvider.this);
            }
        });
        selectionTable.setVisible(false);
        mainPanel.add(selectionTable.getWidget());
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
                retrieverLevelSelectionProvider.removeDimensionFilter(ChooseableDimensionFilterSelectionProvider.this);
                firstChange = false;
            } else {
                // only one dimension remaining for selection; there is no additional dimension selection provider
                // that would remain if we remove the current one; therefore, clear the selection on the current one
                // and see what the user does with it...
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
    
    void fetchAndUpdateAvailableData(final Iterator<ChooseableDimensionFilterSelectionProvider> retrieverLevelSelectionProviderIterator) {
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
            final Iterator<ChooseableDimensionFilterSelectionProvider> retrieverLevelSelectionProviderIterator) {
        final FunctionDTO dimension = getSelectedDimension();
        HashSet<FunctionDTO> dimensionDTOs = new HashSet<>();
        dimensionDTOs.add(dimension);
        HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettingsDTO = retrieverLevelSelectionProvider.getRetrieverSettings();
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionDTO = retrieverLevelSelectionProvider.getCompleteFilterSelection();
        DataRetrieverLevelDTO retrieverLevel = retrieverLevelSelectionProvider.getRetrieverLevel();
        if (filterSelectionDTO.containsKey(retrieverLevel)) {
            filterSelectionDTO.get(retrieverLevel).remove(dimension);
        }
        busyIndicator.setVisible(true);
        counter.increase();
        dataMiningService.getDimensionValuesFor(session, retrieverLevelSelectionProvider.getDataRetrieverChain(),
                retrieverLevel, dimensionDTOs, retrieverSettingsDTO, filterSelectionDTO,
                LocaleInfo.getCurrentLocale().getLocaleName(), new ManagedDataMiningQueryCallback<HashSet<Object>>(counter) {
                    @Override
                    protected void handleSuccess(QueryResultDTO<HashSet<Object>> result) {
                        Map<GroupKey, HashSet<Object>> results = result.getResults();
                        List<Object> content = new ArrayList<Object>();
                        
                        if (!results.isEmpty()) {
                            GroupKey contentKey = new GenericGroupKey<FunctionDTO>(dimension);
                            content.addAll(results.get(contentKey));
                            Collections.sort(content, new Comparator<Object>() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    Comparator<String> naturalComparator = new NaturalComparator();
                                    return naturalComparator.compare(o1.toString(), o2.toString());
                                }
                            });
                        }
                            
                        boolean selectionChanged;
                        if (!isUpdate || content.isEmpty()) {
                            selectionChanged = selectionTable.setContent(content, notifyListenersWhenSelectionChanged);
                        } else {
                            selectionChanged = selectionTable.updateContent(content, notifyListenersWhenSelectionChanged);
                        }
                        updateSelectionTable(notifyListenersWhenSelectionChanged);
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
                    @Override
                    protected void handleFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the dimension values of " + dimension + ": "
                                + caught.getMessage());
                    }
                });
    }
    
    /**
     * If {@code #selectionToBeApplied} is not {@code null}, the table displaying the selection will have its selection
     * model updated based on the contents of {@link #selectionToBeApplied}, and {@link #selectionToBeApplied} will be
     * set to {@code null} afterwards.
     * 
     * @param notifyListenersWhenSelectionChanged
     *            if {@code true}, selection listeners will be notified about any change that is caused by invoking this
     *            method
     */
    private void updateSelectionTable(final boolean notifyListenersWhenSelectionChanged) {
        if (selectionToBeApplied != null) {
            selectionTable.setSelection(selectionToBeApplied, notifyListenersWhenSelectionChanged);
            selectionToBeApplied = null;
        }
    }

    void setAvailableDimensions(Collection<FunctionDTO> availableDimensions) {
        dimensionListBox.setAcceptableValues(availableDimensions);
    }

    public FunctionDTO getSelectedDimension() {
        return dimensionListBox.getValue();
    }

    public HashSet<? extends Serializable> getSelection() {
        return selectionTable.getSelection();
    }
    
    void clearSelection() {
        selectionTable.clearSelection();
    }

    void setSelectedDimensionAndValues(FunctionDTO functionDTO, Collection<?> selection) {
        dimensionChangedHandler.firstChange = false;
        dimensionListBox.setValue(functionDTO, true);
        selectionToBeApplied = selection;
    }
    
    public Widget getEntryWidget() {
        return mainPanel;
    }
    
}