package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.COMPETITOR;

import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
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
    
    private final BoatPanel boatTable;
    private final String regattaName;
    private final Widget widget;
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
        mainPanel.setWidth("100%");
        this.setWidget(mainPanel);
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, COMPETITOR);
        mainPanel.add(buttonPanel);

        // BUTTON - Refresh
        final Button refreshButton = buttonPanel.addUnsecuredAction(stringMessages.refresh(), this::refreshBoatList);
        refreshButton.ensureDebugId("RefreshButton");

        // BUTTON - Add Competitors
        final Button addCompetitorButton = buttonPanel.addCreateAction(stringMessages.add(), null);
        addCompetitorButton.ensureDebugId("AddCompetitorButton");

        // BUTTON - Import Competitors
        buttonPanel.addCreateAction(stringMessages.importCompetitors(), () -> {
            sailingService.getCompetitorProviderNames(new AsyncCallback<Iterable<String>>() {
                @Override
                public void onSuccess(Iterable<String> providerNames) {
                    /*
                    MatchImportedCompetitorsDialogFactory matchCompetitorsDialogFactory = getMatchCompetitorsDialogFactory(
                            sailingService, userService, stringMessages, errorReporter);
                    CompetitorImportProviderSelectionDialog dialog = new CompetitorImportProviderSelectionDialog(
                            matchCompetitorsDialogFactory, CompetitorPanel.this, providerNames, sailingService,
                            stringMessages, errorReporter);
                    dialog.show();
                    */
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter
                            .reportError(stringMessages.errorLoadingCompetitorImportProviders(caught.getMessage()));
                }
            });
        });

        mainPanel.add(busyIndicator);
        mainPanel.add(boatTable);
        
        if (regattaName != null) {
            refreshBoatList();
        }
        
        this.widget = mainPanel;
    }
    
    private void refreshBoatList() {
        // TODO
    }

    @Override
    public void setBusy(boolean isBusy) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Widget getWidget() {
        return widget;
    }

}
