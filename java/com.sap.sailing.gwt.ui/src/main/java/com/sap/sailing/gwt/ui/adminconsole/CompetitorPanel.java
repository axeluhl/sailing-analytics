package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.COMPETITOR;

import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTOImpl;
import com.sap.sailing.gwt.ui.adminconsole.CompetitorImportProviderSelectionDialog.MatchImportedCompetitorsDialogFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
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
    private final String boatClassName;
    private final BusyIndicator busyIndicator;

    public CompetitorPanel(final SailingServiceWriteAsync sailingServiceWrite, final UserService userService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        this(sailingServiceWrite, userService, /* leaderboardName */ null, /* boatClassName */ null, /* createWithBoatByDefault */ true,
                stringMessages, errorReporter);
    }

    public CompetitorPanel(final SailingServiceWriteAsync sailingServiceWrite, final UserService userService, final String leaderboardName,
            String boatClassName, boolean createWithBoatByDefault, final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super();
        this.leaderboardName = leaderboardName;
        this.boatClassName = boatClassName;
        this.competitorTable = new CompetitorTableWrapper<>(sailingServiceWrite, userService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true, 
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
                ()->openAddCompetitorDialog(createWithBoatByDefault));
        addCompetitorButton.ensureDebugId("AddCompetitorButton");
        buttonPanel.addUnsecuredAction(stringMessages.selectAll(), () -> {
            for (CompetitorDTO c : competitorTable.getDataProvider().getList()) {
                refreshableCompetitorSelectionModel.setSelected(c, true);
            }
        });
        buttonPanel.addCreateAction(stringMessages.importCompetitors(), () -> {
            sailingServiceWrite.getCompetitorProviderNames(new AsyncCallback<Iterable<String>>() {
                @Override
                public void onSuccess(Iterable<String> providerNames) {
                    MatchImportedCompetitorsDialogFactory matchCompetitorsDialogFactory = getMatchCompetitorsDialogFactory(
                            sailingServiceWrite, userService, stringMessages, errorReporter);
                    CompetitorImportProviderSelectionDialog dialog = new CompetitorImportProviderSelectionDialog(
                            matchCompetitorsDialogFactory, CompetitorPanel.this, providerNames, sailingServiceWrite,
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
                final CompetitorInvitationHelper helper = new CompetitorInvitationHelper(sailingServiceWrite, stringMessages,
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
            final SailingServiceWriteAsync sailingServiceWrite, final UserService userService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        return new MatchImportedCompetitorsDialogFactory() {
            @Override
            public MatchImportedCompetitorsDialog createMatchImportedCompetitorsDialog(
                    final Iterable<CompetitorDescriptor> competitorDescriptors,
                    final Iterable<CompetitorDTO> competitors) {
                ImportCompetitorCallback importCompetitorCallback = new ImportCompetitorCallback(sailingServiceWrite, errorReporter, stringMessages) {
                    @Override
                    public void registerCompetitors(Set<CompetitorDTO> competitorDTOs) {
                        super.registerCompetitors(competitorDTOs);
                        refreshCompetitorList();
                    }
                };
                return new MatchImportedCompetitorsDialog(competitorDescriptors, competitors, stringMessages,
                        sailingServiceWrite, userService, errorReporter, importCompetitorCallback);
            }
        };
    }

    private void openAddCompetitorDialog(boolean createWithBoatByDefault) {
        CompetitorWithBoatDTOImpl competitorDTO = new CompetitorWithBoatDTOImpl();
        BoatClassDTO boatClassDTO = new BoatClassDTO(boatClassName, /* hullLength */ null, /* hullBeam */ null);
        BoatDTO boatDTO = new BoatDTO();
        boatDTO.setBoatClass(boatClassDTO);
        competitorDTO.setBoat(boatDTO);
        competitorTable.openCompetitorWithBoatAddDialog(competitorDTO, createWithBoatByDefault);
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
