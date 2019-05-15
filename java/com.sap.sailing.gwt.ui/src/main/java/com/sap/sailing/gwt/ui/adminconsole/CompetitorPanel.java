package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.COMPETITOR;

import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTOImpl;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorImportProviderSelectionDialog.MatchImportedCompetitorsDialogFactory;
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
 * Allows an administrator to view and edit the set of competitors currently maintained by the server.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorPanel extends SimplePanel implements BusyDisplay {
    private final CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> competitorTable;
    private final RefreshableMultiSelectionModel<CompetitorDTO> refreshableCompetitorSelectionModel;
    private final String leaderboardName;

    private final BusyIndicator busyIndicator;

    public CompetitorPanel(final SailingServiceAsync sailingService, final UserService userService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        this(sailingService, userService, null, stringMessages, errorReporter);
    }

    public CompetitorPanel(final SailingServiceAsync sailingService, final UserService userService, final String leaderboardName,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super();
        this.leaderboardName = leaderboardName;
        this.competitorTable = new CompetitorTableWrapper<>(sailingService, userService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true, 
                /* filterCompetitorWithBoat */ false, /* filterCompetitorsWithoutBoat */ false);
        this.refreshableCompetitorSelectionModel = (RefreshableMultiSelectionModel<CompetitorDTO>) competitorTable.getSelectionModel();
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        this.setWidget(mainPanel);
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, COMPETITOR);
        mainPanel.add(buttonPanel);

        final Button refreshButton = buttonPanel.addUnsecuredAction(stringMessages.refresh(),
                this::refreshCompetitorList);
        refreshButton.ensureDebugId("RefreshButton");

        final Button allowReloadButton = buttonPanel.addUnsecuredAction(stringMessages.allowReload(),
                () -> competitorTable.allowUpdate(refreshableCompetitorSelectionModel.getSelectedSet()));
        refreshableCompetitorSelectionModel.addSelectionChangeHandler(
                event -> allowReloadButton.setEnabled(!refreshableCompetitorSelectionModel.getSelectedSet().isEmpty()));
        allowReloadButton.setEnabled(!refreshableCompetitorSelectionModel.getSelectedSet().isEmpty());

        final Button addCompetitorButton = buttonPanel.addCreateAction(stringMessages.add(),
                this::openAddCompetitorDialog);
        addCompetitorButton.ensureDebugId("AddCompetitorButton");
        
        buttonPanel.addUnsecuredAction(stringMessages.selectAll(), () -> {
            for (CompetitorDTO c : competitorTable.getDataProvider().getList()) {
                refreshableCompetitorSelectionModel.setSelected(c, true);
            }
        });

        buttonPanel.addCreateAction(stringMessages.importCompetitors(), () -> {
            sailingService.getCompetitorProviderNames(new AsyncCallback<Iterable<String>>() {
                @Override
                public void onSuccess(Iterable<String> providerNames) {
                    MatchImportedCompetitorsDialogFactory matchCompetitorsDialogFactory = getMatchCompetitorsDialogFactory(
                            sailingService, userService, stringMessages, errorReporter);
                    CompetitorImportProviderSelectionDialog dialog = new CompetitorImportProviderSelectionDialog(
                            matchCompetitorsDialogFactory, CompetitorPanel.this, providerNames, sailingService,
                            stringMessages, errorReporter);
                    dialog.show();
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter
                            .reportError(stringMessages.errorLoadingCompetitorImportProviders(caught.getMessage()));
                }
            });
        });

        // only if this competitor panel is connected to a leaderboard, we want to enable invitations
        if (leaderboardName != null) {
            buttonPanel.addCreateAction(stringMessages.inviteSelectedCompetitors(), () -> {
                final Set<CompetitorDTO> competitors = refreshableCompetitorSelectionModel.getSelectedSet();
                final CompetitorInvitationHelper helper = new CompetitorInvitationHelper(sailingService, stringMessages,
                        errorReporter);
                helper.inviteCompetitors(competitors, leaderboardName);
            });
        }

        mainPanel.add(busyIndicator);
        mainPanel.add(competitorTable);
        
        if (leaderboardName != null) {
            refreshCompetitorList();
        }
    }

    private MatchImportedCompetitorsDialogFactory getMatchCompetitorsDialogFactory(
            final SailingServiceAsync sailingService, final UserService userService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        return new MatchImportedCompetitorsDialogFactory() {
            @Override
            public MatchImportedCompetitorsDialog createMatchImportedCompetitorsDialog(
                    final Iterable<CompetitorDescriptor> competitorDescriptors,
                    final Iterable<CompetitorDTO> competitors) {
                ImportCompetitorCallback importCompetitorCallback = new ImportCompetitorCallback(sailingService, errorReporter, stringMessages) {
                    @Override
                    public void registerCompetitors(Set<CompetitorDTO> competitorDTOs) {
                        super.registerCompetitors(competitorDTOs);
                        refreshCompetitorList();
                    }
                };
                return new MatchImportedCompetitorsDialog(competitorDescriptors, competitors, stringMessages,
                        sailingService, userService, errorReporter, importCompetitorCallback);
            }
        };
    }

    private void openAddCompetitorDialog() {
        CompetitorWithBoatDTOImpl competitorDTO = new CompetitorWithBoatDTOImpl();
        competitorDTO.setBoat(null);
        competitorTable.openEditCompetitorWithoutBoatDialog(competitorDTO);
    }

    public void refreshCompetitorList() {
        competitorTable.refreshCompetitorList(leaderboardName);
    }

    @Override
    public void setBusy(boolean isBusy) {
        if (busyIndicator.isBusy() != isBusy) {
            busyIndicator.setBusy(isBusy);
        }
    }
}
