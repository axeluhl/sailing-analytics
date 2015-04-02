package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
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

class SingleDimensionFilterSelectionProvider {
    
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final SingleRetrieverLevelSelectionProvider retrieverLevelSelectionProvider;

    private final FlowPanel mainPanel;
    private final ValueListBox<FunctionDTO> dimensionListBox;
    private final SimpleBusyIndicator busyIndicator;
    private final FilterableSelectionTable<?> selectionTable;
    
    public SingleDimensionFilterSelectionProvider(DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter, DataMiningSession session,
                                                  SingleRetrieverLevelSelectionProvider retrieverLevelSelectionProvider) {
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.session = session;
        this.retrieverLevelSelectionProvider = retrieverLevelSelectionProvider;
        
        mainPanel = new FlowPanel();
        
        dimensionListBox = new ValueListBox<FunctionDTO>(new AbstractObjectRenderer<FunctionDTO>() {
            @Override
            protected String convertObjectToString(FunctionDTO function) {
                return function.getDisplayName();
            }
        });
        dimensionListBox.addValueChangeHandler(new DimensionChangedHandler());
        mainPanel.add(dimensionListBox);
        
        busyIndicator = new SimpleBusyIndicator(true, 0.7f);
        busyIndicator.setVisible(false);
        mainPanel.add(busyIndicator);
        
        selectionTable = new FilterableSelectionTable<>();
        mainPanel.add(selectionTable);
    }
    
    private class DimensionChangedHandler implements ValueChangeHandler<FunctionDTO> {
        @Override
        public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
            final FunctionDTO dimension = getSelectedDimension();
            selectionTable.setVisible(false);
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
                                selectionTable.updateContent(content == null ? new ArrayList<>() : content);
                                
                                busyIndicator.setVisible(false);
                                selectionTable.setVisible(true);
                                
                                //TODO Add a new dimension filter to the retrieverLevelSelectionProvider
                            }
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error fetching the dimension values of " + dimension + ": "
                                        + caught.getMessage());
                            }
                        });
            } else {
                //TODO Remove this dimension filter from the retrieverLevelSelectionProvider
            }
        }
    }
    
    void setAvailableDimensions(Collection<FunctionDTO> availableDimensions) {
        dimensionListBox.setAcceptableValues(availableDimensions);
    }

    public FunctionDTO getSelectedDimension() {
        return dimensionListBox.getValue();
    }
    
    void setSelection(Iterable<?> elements) {
        selectionTable.setSelection(elements);
    }

    public Collection<? extends Serializable> getSelection() {
        return selectionTable.getSelection();
    }
    
    void clearSelection() {
        selectionTable.clearSelection();
    }
    
    void addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        selectionTable.addSelectionChangeHandler(handler);
    }
    
    public Widget getEntryWidget() {
        return mainPanel;
    }

    public void resizeToHeight(int heightInPX) {
        int widthInPX = dimensionListBox.getOffsetWidth();
        mainPanel.setSize(widthInPX + "px", heightInPX + "px");
        
        int remainingHeightInPX = Math.max(0, heightInPX - dimensionListBox.getOffsetHeight());
        selectionTable.resizeTo(widthInPX, remainingHeightInPX);
    }
    
}