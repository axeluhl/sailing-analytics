package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class ORCLegEventCreationDialog extends DataEntryDialog<RaceLogORCLegDataEvent> {

    private final TextBox length;
    private final TextBox twa;
    private final TextBox tws;
    private final ListBox legType;
    
    public ORCLegEventCreationDialog(String title, String message, String okButtonName, String cancelButtonName,
            Validator<RaceLogORCLegDataEvent> validator, DialogCallback<RaceLogORCLegDataEvent> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        twa = null;
        tws = null;
        legType = null;
        length = null;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected RaceLogORCLegDataEvent getResult() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(4,1);
        HorizontalPanel lengthRow = new HorizontalPanel();
        lengthRow.add(new Label("Length in NM"));
        lengthRow.add(length);
        grid.setWidget(0, 0, lengthRow);
        return grid;
    }
    
}
