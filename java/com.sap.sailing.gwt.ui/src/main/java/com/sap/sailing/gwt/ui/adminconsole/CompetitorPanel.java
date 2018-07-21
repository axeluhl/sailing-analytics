package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
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

    public CompetitorPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        this(sailingService, null, stringMessages, errorReporter);
    }

    public CompetitorPanel(final SailingServiceAsync sailingService, final String leaderboardName,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super();
        this.leaderboardName = leaderboardName;
        this.competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true, 
                /* filterCompetitorWithBoat */ false, /* filterCompetitorsWithoutBoat */ false);
        this.refreshableCompetitorSelectionModel = (RefreshableMultiSelectionModel<CompetitorDTO>) competitorTable.getSelectionModel();
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        HorizontalPanel competitorsPanel = new HorizontalPanel();
        competitorsPanel.setSpacing(5);
        mainPanel.add(competitorsPanel);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshCompetitorList();
            }
        });
        refreshButton.ensureDebugId("RefreshButton");
        buttonPanel.add(refreshButton);
        final Button allowReloadButton = new Button(stringMessages.allowReload());
        allowReloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                competitorTable.allowUpdate(refreshableCompetitorSelectionModel.getSelectedSet());
            }
        });
        buttonPanel.add(allowReloadButton);
        Button addCompetitorButton = new Button(stringMessages.add());
        addCompetitorButton.ensureDebugId("AddCompetitorButton");
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openAddCompetitorDialog();
            }
        });
        buttonPanel.add(addCompetitorButton);
        
        Button selectAllButton = new Button(stringMessages.selectAll());
        selectAllButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (CompetitorDTO c : competitorTable.getDataProvider().getList()) {
                    refreshableCompetitorSelectionModel.setSelected(c, true);
                }
            }
        });
        buttonPanel.add(selectAllButton);

        Button competitorImportButton = new Button(stringMessages.importCompetitors());
        competitorImportButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sailingService.getCompetitorProviderNames(new AsyncCallback<Iterable<String>>() {
                    @Override
                    public void onSuccess(Iterable<String> providerNames) {
                        MatchImportedCompetitorsDialogFactory matchCompetitorsDialogFactory = getMatchCompetitorsDialogFactory(
                                sailingService, stringMessages, errorReporter);
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
            }
        });
        buttonPanel.add(competitorImportButton);

        //only if this competitor panel is connected to a leaderboard, we want to enable invitations
        if (leaderboardName != null) {
            final Button inviteCompetitorsButton = new Button(stringMessages.inviteSelectedCompetitors());
            inviteCompetitorsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Set<CompetitorDTO> competitors = refreshableCompetitorSelectionModel.getSelectedSet();
                    CompetitorInvitationHelper helper = new CompetitorInvitationHelper(sailingService, stringMessages, errorReporter);
                    helper.inviteCompetitors(competitors, leaderboardName);
                }
            });
            buttonPanel.add(inviteCompetitorsButton);
        }

        competitorsPanel.add(buttonPanel);
        mainPanel.add(busyIndicator);
        mainPanel.add(competitorTable);
        
        refreshableCompetitorSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                allowReloadButton.setEnabled(!refreshableCompetitorSelectionModel.getSelectedSet().isEmpty());
            }
        });
        allowReloadButton.setEnabled(!refreshableCompetitorSelectionModel.getSelectedSet().isEmpty());

        if (leaderboardName != null) {
            refreshCompetitorList();
        }
    }

    private MatchImportedCompetitorsDialogFactory getMatchCompetitorsDialogFactory(
            final SailingServiceAsync sailingService, final StringMessages stringMessages,
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
                        sailingService, errorReporter, importCompetitorCallback);
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
