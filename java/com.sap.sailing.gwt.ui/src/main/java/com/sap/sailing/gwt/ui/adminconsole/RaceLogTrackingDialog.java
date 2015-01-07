package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class RaceLogTrackingDialog extends AbstractSaveDialog {
    protected final String leaderboardName;
    protected final String raceColumnName;
    protected final String fleetName;
    public RaceLogTrackingDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, String leaderboardName, String raceColumnName, String fleetName) {
        this(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName, true);
    }
    
    public RaceLogTrackingDialog(SailingServiceAsync sailingService, StringMessages stringMessages,
            ErrorReporter errorReporter, String leaderboardName, String raceColumnName, String fleetName, boolean editable) {
        super(sailingService, stringMessages, errorReporter, editable);
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
    }
}
