package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;

public class ControlPointTableWrapper<S extends RefreshableSelectionModel<ControlPointDTO>> extends TableWrapper<ControlPointDTO, S> {    
    public ControlPointTableWrapper(boolean multiSelection, SailingServiceWriteAsync sailingServiceWrite, final StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingServiceWrite, stringMessages, errorReporter, multiSelection, /* enablePager */ true,
                new EntityIdentityComparator<ControlPointDTO>() {
                    @Override
                    public boolean representSameEntity(ControlPointDTO dto1, ControlPointDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(ControlPointDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                });
        TextColumn<ControlPointDTO> nameColumn = new TextColumn<ControlPointDTO>() {
            @Override
            public String getValue(ControlPointDTO d) {
                return d.getName();
            }
        };
        table.addColumn(nameColumn, stringMessages.name());
        
        TextColumn<ControlPointDTO> shortNameColumn = new TextColumn<ControlPointDTO>() {
            @Override
            public String getValue(ControlPointDTO d) {
                return d.getShortName();
            }
        };
        table.addColumn(shortNameColumn, stringMessages.shortName());

        TextColumn<ControlPointDTO> typeColumn = new TextColumn<ControlPointDTO>() {
            @Override
            public String getValue(ControlPointDTO d) {
                if (d instanceof GateDTO) {
                    return stringMessages.twoXMark();
                } else if (d instanceof MarkDTO) {
                    return stringMessages.mark();
                } else {
                    return stringMessages.unknown();
                }
            }
        };
        table.addColumn(typeColumn, stringMessages.type());
    }
}
