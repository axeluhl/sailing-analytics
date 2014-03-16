package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

public class RaceLogTrackingCompetitorRegistrationsDialog extends RaceLogTrackingDialog {
    private CompetitorTableWrapper competitorTable;
    private final boolean filterByLeaderBoardInitially = false;

    public RaceLogTrackingCompetitorRegistrationsDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName,
            final String raceColumnName, final String fleetName, boolean editable) {
        super(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName, editable);

        competitorTable.refreshCompetitorList(filterByLeaderBoardInitially ? leaderboardName : null,
                true, new Callback<Iterable<CompetitorDTO>, Throwable>() {
            @Override
            public void onSuccess(Iterable<CompetitorDTO> result) {
                center();

                //preselect competitors that are already registered
                sailingService.getCompetitorRegistrations(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Iterable<CompetitorDTO>>() {
                    @Override
                    public void onSuccess(Iterable<CompetitorDTO> result) {
                        Set<String> registeredIds = new HashSet<String>();
                        for (CompetitorDTO c : result) {
                            registeredIds.add(c.getIdAsString());
                        }
                        for (CompetitorDTO c : competitorTable.getAllCompetitors()) {
                            competitorTable.getSelectionModel().setSelected(c, registeredIds.contains(c.getIdAsString()));
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not load already registered competitors: " + caught.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Throwable reason) {}
        });
    }

    @Override
    protected void addButtons(Panel buttonPanel) {
        final Button toggleOnlyLeaderboard = new Button(filterByLeaderBoardInitially ? stringMessages.removeLeaderboardFilter(): 
            stringMessages.filterByLeaderboard());
        toggleOnlyLeaderboard.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean filterByLeaderboard = toggleOnlyLeaderboard.getText().equals(stringMessages.filterByLeaderboard());
                competitorTable.refreshCompetitorList(filterByLeaderboard ? leaderboardName : null, true);
                toggleOnlyLeaderboard.setText(filterByLeaderboard ? stringMessages.removeLeaderboardFilter() :
                    stringMessages.filterByLeaderboard());
            }
        });
        final Button toggleSelection = new Button(stringMessages.selectAll());
        toggleSelection.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean selectAll = toggleSelection.getText().equals(stringMessages.selectAll());
                for (CompetitorDTO competitor : competitorTable.getAllCompetitors()) {
                    competitorTable.getSelectionModel().setSelected(competitor, selectAll);
                }
                toggleSelection.setText(selectAll ? stringMessages.deselectAll() : stringMessages.selectAll());
            }

        });
        buttonPanel.add(toggleOnlyLeaderboard);
        buttonPanel.add(toggleSelection);
        

        Button addCompetitorButton = new Button(stringMessages.add());
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openAddCompetitorDialog();
            }
        });
        buttonPanel.add(addCompetitorButton);

        super.addButtons(buttonPanel);
    }

    @Override
    protected void addMainContent(Panel mainPanel) {
        super.addMainContent(mainPanel);

        competitorTable = new CompetitorTableWrapper(sailingService, stringMessages, errorReporter);
        mainPanel.add(competitorTable);
    }

    @Override
    protected void save() {
        sailingService.setCompetitorRegistrations(leaderboardName, raceColumnName, fleetName,
                competitorTable.getSelectionModel().getSelectedSet(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                hide();
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not save competitor registrations: " + caught.getMessage());
            }
        });
    }

    private void openAddCompetitorDialog() {
        new CompetitorEditDialog(stringMessages, new CompetitorDTOImpl(), new DialogCallback<CompetitorDTO>() {
            @Override
            public void ok(CompetitorDTO competitor) {
                sailingService.addOrUpdateCompetitor(competitor, new AsyncCallback<CompetitorDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to add competitor: "+caught.getMessage());
                    }

                    @Override
                    public void onSuccess(CompetitorDTO updatedCompetitor) {
                        competitorTable.getAllCompetitors().add(updatedCompetitor);
                        competitorTable.getFilterField().updateAll(competitorTable.getAllCompetitors());
                    }
                });
            }

            @Override
            public void cancel() {
            }
        }).show();
    }
}
