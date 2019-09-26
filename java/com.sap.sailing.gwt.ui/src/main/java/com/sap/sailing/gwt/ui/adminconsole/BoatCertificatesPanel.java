package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.COMPETITOR;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
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
    
    private final BoatWithCertificateTableWrapper<RefreshableMultiSelectionModel<BoatDTO>> boatTable;
    @SuppressWarnings("unused")
    private final RefreshableMultiSelectionModel<BoatDTO> refreshableBoatSelectionModel;
    @SuppressWarnings("unused")
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
        
        this.boatTable = new BoatWithCertificateTableWrapper<>(sailingService, userService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true, 100, true);
        this.refreshableBoatSelectionModel = (RefreshableMultiSelectionModel<BoatDTO>) boatTable.getSelectionModel();
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        Grid tablesPanel = new Grid(1,2);
        tablesPanel.setWidth("100%");
        final AccessControlledButtonPanel topButtonPanel = new AccessControlledButtonPanel(userService, COMPETITOR);
        mainPanel.add(topButtonPanel);
        mainPanel.add(tablesPanel);

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
        
        if (regattaName != null) {
            refreshBoatList();
        }        
    }
    
    private void refreshBoatList() {
        boatTable.refreshBoatList(/* loadOnlyStandaloneBoats */ false, /* callback */ null);
    }

    @Override
    public void setBusy(boolean isBusy) {
        // TODO Auto-generated method stub
        
    }
}
