package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.COMPETITOR;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyDisplay;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

/**
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class BoatCertificatesPanel extends SimplePanel implements BusyDisplay {
    
    private final BoatPanel boatTable;
    private final String regattaName;
    private final BusyIndicator busyIndicator;

    public BoatCertificatesPanel(final SailingServiceAsync sailingService, final UserService userService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        this(sailingService, userService, null, stringMessages, errorReporter);
    }
    
    public BoatCertificatesPanel(final SailingServiceAsync sailingService, final UserService userService, final String regattaName,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super();
        this.regattaName = regattaName;
        
        this.boatTable = new BoatPanel(sailingService, userService, stringMessages, errorReporter);
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        Grid tablesPanel = new Grid(1,2);
        tablesPanel.setWidth("100%");
        final AccessControlledButtonPanel topButtonPanel = new AccessControlledButtonPanel(userService, COMPETITOR);
        final AccessControlledButtonPanel bottomButtonPanel = new AccessControlledButtonPanel(userService, COMPETITOR);
        mainPanel.add(topButtonPanel);
        mainPanel.add(tablesPanel);
        mainPanel.add(bottomButtonPanel);

        // BUTTON - Refresh
        final Button refreshButton = topButtonPanel.addUnsecuredAction(stringMessages.refresh(), this::refreshBoatList);
        refreshButton.ensureDebugId("RefreshButton");

        // BUTTON - Import Certificates
        final Button importCertificatesButton = topButtonPanel.addCreateAction(stringMessages.importCertificates(), null);
        importCertificatesButton.ensureDebugId("AddCompetitorButton");

        // TABLE - Boats
        VerticalPanel boatPanel = new VerticalPanel();
        boatPanel.add(busyIndicator);
        boatPanel.add(boatTable);
        CaptionPanel boatCaptionPanel = new CaptionPanel("Boats");
        boatCaptionPanel.add(boatPanel);
        tablesPanel.setWidget(0, 0, boatCaptionPanel);

        // TABLE - Certificates
        tablesPanel.setWidget(0, 1, new CaptionPanel("Certificates"));
        
        // BUTTON - Link
        final Button linkButton = bottomButtonPanel.addCreateAction("Link", null);
        linkButton.ensureDebugId("LinkCertificateToBoatButton");
        
        // BUTTON - Unlink
        final Button unlinkButton = bottomButtonPanel.addCreateAction("Unlink", null);
        linkButton.ensureDebugId("UnlinkCertificateToBoatButton");
        
        if (regattaName != null) {
            refreshBoatList();
        }        
    }
    
    private Widget getBoatTable() {
        return null;
    }
    
    private Widget getCertificatesTable() {
        return null;
    }
    
    private void refreshBoatList() {
        // TODO
    }

    @Override
    public void setBusy(boolean isBusy) {
        // TODO Auto-generated method stub
        
    }
}
