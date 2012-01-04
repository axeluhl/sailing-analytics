package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public class Spectator extends AbstractEntryPoint implements EventRefresher {
    
    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        rootPanel.setSize("95%", "95%");
        
        TabPanel tabPanel = new TabPanel();
        tabPanel.setAnimationEnabled(true);
        rootPanel.add(tabPanel, 10, 10);
        tabPanel.setSize("95%", "95%");
        
        OverviewEventManagementPanel overviewEventManagementPanel = new OverviewEventManagementPanel(sailingService, this, this, stringConstants);
        overviewEventManagementPanel.setSize("90%", "90%");
        tabPanel.add(overviewEventManagementPanel, "Overview", false);
        
        tabPanel.selectTab(0);
        fillEvents();
    }

    @Override
    public void fillEvents() {
        
    }
    
}
