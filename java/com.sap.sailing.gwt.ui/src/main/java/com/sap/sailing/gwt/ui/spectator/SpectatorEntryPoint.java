package com.sap.sailing.gwt.ui.spectator;

import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.EventRefresher;

/**
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public class SpectatorEntryPoint extends AbstractEntryPoint implements EventRefresher {
    
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("100%", "100%");
        
        OverviewEventPanel overviewEventManagementPanel = new OverviewEventPanel(sailingService, this, this, stringConstants);
        overviewEventManagementPanel.setSize("100%", "100%");
        rootPanel.add(overviewEventManagementPanel);

        fillEvents();
    }

    @Override
    public void fillEvents() {
        
    }
    
}
