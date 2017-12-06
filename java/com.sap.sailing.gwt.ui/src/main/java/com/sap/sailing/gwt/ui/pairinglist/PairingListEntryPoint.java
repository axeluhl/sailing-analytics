package com.sap.sailing.gwt.ui.pairinglist;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;

public class PairingListEntryPoint extends AbstractSailingEntryPoint {
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();

        this.createUI();
    }
    
    private void createUI() {
        DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
        RootLayoutPanel.get().add(mainPanel);
        
        SAPSailingHeaderWithAuthentication header = new SAPSailingHeaderWithAuthentication("PairingList");
        new FixedSailingAuthentication(getUserService(), header.getAuthenticationMenuView());
        mainPanel.addNorth(header, 75);
        
        mainPanel.add(new ScrollPanel(createPairingListPanel()));
    }
    
    private Widget createPairingListPanel() {
        VerticalPanel pairingListPanel = new VerticalPanel();
        
        // TODO show pairing list
        
        return pairingListPanel;
    }
}
