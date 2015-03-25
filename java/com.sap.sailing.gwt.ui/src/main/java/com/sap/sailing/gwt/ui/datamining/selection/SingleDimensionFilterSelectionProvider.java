package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.Collection;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.AbstractObjectRenderer;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.gwt.client.ErrorReporter;

class SingleDimensionFilterSelectionProvider {
    
//    private final StringMessages stringMessages;
//    private final DataMiningServiceAsync dataMiningService;
//    private final ErrorReporter errorReporter;
//    private final DataMiningSession session;
//    private final SingleRetrieverLevelSelectionProvider retrieverLevelSelectionProvider;

    private final DockLayoutPanel mainPanel;
    private final ValueListBox<FunctionDTO> dimensionListBox;
    private final SimpleSelectionTable<?> selectionTable;
    
    public SingleDimensionFilterSelectionProvider(StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter,
                                                  DataMiningSession session, SingleRetrieverLevelSelectionProvider retrieverLevelSelectionProvider) {
//        this.stringMessages = stringMessages;
//        this.dataMiningService = dataMiningService;
//        this.errorReporter = errorReporter;
//        this.session = session;
//        this.retrieverLevelSelectionProvider = retrieverLevelSelectionProvider;
        
        mainPanel = new DockLayoutPanel(Unit.PX);
        
        dimensionListBox = new ValueListBox<FunctionDTO>(new AbstractObjectRenderer<FunctionDTO>() {
            @Override
            protected String convertObjectToString(FunctionDTO function) {
                return function.getDisplayName();
            }
        });
        dimensionListBox.addValueChangeHandler(new DimensionChangedHandler());
        mainPanel.addNorth(dimensionListBox, 30);
        
        selectionTable = new SimpleSelectionTable<>();
        mainPanel.add(selectionTable);
    }
    
    private class DimensionChangedHandler implements ValueChangeHandler<FunctionDTO> {
        @Override
        public void onValueChange(ValueChangeEvent<FunctionDTO> event) {
            // TODO Auto-generated method stub
            
        }
    }

    public FunctionDTO getDimension() {
        return dimensionListBox.getValue();
    }
    
    public void setSelection(Iterable<?> elements) {
        selectionTable.setSelection(elements);
    }

    public Collection<? extends Serializable> getSelection() {
        return selectionTable.getSelectionAsValues();
    }
    
    public void clearSelection() {
        selectionTable.clearSelection();
    }

    public Widget getEntryWidget() {
        return mainPanel;
    }
    
}