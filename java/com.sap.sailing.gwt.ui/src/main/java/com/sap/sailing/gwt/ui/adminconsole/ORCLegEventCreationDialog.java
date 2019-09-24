package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

// TODO unfinished, first we need a concept how to synchronize the leg/course values with the server values
// additionally probably we will need a RaceLogORCLegDataEventDTO 
public class ORCLegEventCreationDialog extends DataEntryDialog<RaceLogORCLegDataEvent> {

    private final TextBox length;
    private final TextBox twa;
    private final TextBox tws;
    private final ListBox legType;
    
    public ORCLegEventCreationDialog(String title, String message, String okButtonName, String cancelButtonName,
            Validator<RaceLogORCLegDataEvent> validator, DialogCallback<RaceLogORCLegDataEvent> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        twa = createTextBox("");
        tws = createTextBox("");
        length = createTextBox("");
        legType = new ListBox();
        legType.addItem("TWA");
        legType.addItem("Windward/Leeward");
        legType.addItem("Coastal/Long Distance");
        legType.addItem("Circular Random");
        legType.addItem("Non-Spinnaker");
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
        HorizontalPanel twaRow = new HorizontalPanel();
        twaRow.add(new Label("TWA (difference between course angle and true wind direction)"));
        twaRow.add(twa);
        grid.setWidget(1, 0, twaRow);
        HorizontalPanel twsRow = new HorizontalPanel();
        twsRow.add(new Label("TWS (Implied Wind correction)"));
        twsRow.add(tws);
        grid.setWidget(2, 0, twsRow);
        HorizontalPanel legTypeRow = new HorizontalPanel();
        legTypeRow.add(new Label("Leg Type"));
        legTypeRow.add(legType);
        grid.setWidget(3, 0, legTypeRow);
        return grid;
    }
    
}
