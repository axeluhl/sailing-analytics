package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

public class ControlPointTableWrapper<S extends SelectionModel<ControlPointDTO>> extends TableWrapper<ControlPointDTO, S> {    
    public ControlPointTableWrapper(boolean multiSelection, SailingServiceAsync sailingService, final StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, multiSelection, /* enablePager */ true,
                new EntityIdentityComparator<ControlPointDTO>() {

                    @Override
                    public boolean representSameEntity(ControlPointDTO dto1, ControlPointDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                });
        TextColumn<ControlPointDTO> nameColumn = new TextColumn<ControlPointDTO>() {
            @Override
            public String getValue(ControlPointDTO d) {
                return d.getName();
            }
        };
        table.addColumn(nameColumn, stringMessages.name());
        
        TextColumn<ControlPointDTO> typeColumn = new TextColumn<ControlPointDTO>() {
            @Override
            public String getValue(ControlPointDTO d) {
                if (d instanceof GateDTO) {
                    return "2x " + stringMessages.mark();
                } else if (d instanceof MarkDTO) {
                    return stringMessages.mark();
                } else {
                    return stringMessages.unknown();
                }
            }
        };
        table.addColumn(typeColumn, stringMessages.type());
    }
    @Override
    public void refresh(Iterable<ControlPointDTO> controllPoints) {
        super.refresh(controllPoints);
        ((RefreshableSelectionModel<ControlPointDTO>) getSelectionModel()).refreshSelectionModel(controllPoints);
    }
}
