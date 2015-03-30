package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class ControlPointTableWrapper<S extends SelectionModel<ControlPointDTO>> extends TableWrapper<ControlPointDTO, S> {    
    public ControlPointTableWrapper(boolean multiSelection, SailingServiceAsync sailingService, final StringMessages stringMessages,
            ErrorReporter errorReporter) {
        super(sailingService, stringMessages, errorReporter, multiSelection, /* enablePager */ true);
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
}
