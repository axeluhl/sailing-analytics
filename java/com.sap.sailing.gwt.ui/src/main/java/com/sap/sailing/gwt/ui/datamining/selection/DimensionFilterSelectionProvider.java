package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.shared.components.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
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
        selectionTable.setVisible(false);
        mainPanel.add(selectionTable);
    }
    
    private class DimensionChangedHandler implements ValueChangeHandler<FunctionDTO> {
        boolean firstChange = true;
        Collection<?> selectionToBeApplied;
        
        @Override
        public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
            final FunctionDTO dimension = event.getValue();
            if (dimension != null) {
                Collection<FunctionDTO> dimensionDTOs = new ArrayList<>();
                dimensionDTOs.add(dimension);
                busyIndicator.setVisible(true);
                dataMiningService.getDimensionValuesFor(session, retrieverLevelSelectionProvider
                        .getDataRetrieverChain(), retrieverLevelSelectionProvider.getRetrieverLevel(), dimensionDTOs,
                        LocaleInfo.getCurrentLocale().getLocaleName(), new AsyncCallback<QueryResult<Set<Object>>>() {
                            @Override
                            public void onSuccess(QueryResult<Set<Object>> result) {
                                GroupKey contentKey = new GenericGroupKey<FunctionDTO>(dimension);
                                Collection<?> content = result.getResults().get(contentKey);
                                selectionTable.setContent(content == null ? new ArrayList<>() : content);
                                if (selectionToBeApplied != null) {
                                    selectionTable.setSelection(selectionToBeApplied);
                                    selectionToBeApplied = null;
                                }
                                
                                busyIndicator.setVisible(false);
                                selectionTable.setVisible(true);
                                toggleFilterButton.setVisible(true);
                            }
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error fetching the dimension values of " + dimension + ": "
                                        + caught.getMessage());
                            }
                        });
                if (firstChange && retrieverLevelSelectionProvider.canAddDimensionFilter()) {
                    retrieverLevelSelectionProvider.createAndAddDimensionFilter();
                }
                firstChange = false;
            } else if (retrieverLevelSelectionProvider.shouldRemoveDimensionFilter()) {
                retrieverLevelSelectionProvider.removeDimensionFilter(DimensionFilterSelectionProvider.this);
                firstChange = false;
            } else {
                selectionTable.clearSelection();
                selectionTable.setContent(new ArrayList<>());
                
                selectionTable.setVisible(false);
                toggleFilterButton.setVisible(false);
                firstChange = true;
            }
            retrieverLevelSelectionProvider.updateAvailableDimensions();
        }
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
        dimensionChangedHandler.selectionToBeApplied = selection;
        dimensionListBox.setValue(functionDTO, true);
    }
    
    void addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        selectionTable.addSelectionChangeHandler(handler);
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