package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;

/**
 * Allows an administrator to view the boats assigned to the competitors of a race.
 * 
 * @author Frank Mittag
 * 
 */
public class CompetitorToBoatMappingsViewPanel extends SimplePanel {
    private final CompactCompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;
    private final CompactBoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> boatTable;
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    
    private final RefreshableSelectionModel<CompetitorDTO> refreshableCompetitorSelectionModel;

    public CompetitorToBoatMappingsViewPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName, final String raceColumnName, final String fleetName) {
        super();
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.errorReporter = errorReporter;
        this.competitorTable = new CompactCompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ false, /* enablePager */ true);
        this.boatTable = new CompactBoatTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ false, /* enablePager */ true);
        refreshableCompetitorSelectionModel = competitorTable.getSelectionModel();
        refreshableCompetitorSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                // If the selection on the competitorTable changes,
                // you don't want to link or unlink competitors with the
                // boatListHandler.
                competitorSelectionChanged();
            }
        });
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        CaptionPanel competitorsPanel = new CaptionPanel(stringMessages.competitors());
        competitorsPanel.ensureDebugId("CompetitorsSection");
        competitorsPanel.setContentWidget(this.competitorTable.asWidget());
        CaptionPanel boatsPanel = new CaptionPanel(stringMessages.boats());
        boatsPanel.ensureDebugId("BoatsSection");
        boatsPanel.setContentWidget(this.boatTable.asWidget());
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        mainPanel.add(buttonPanel);
        Grid grid = new Grid(1, 2);
        grid.setWidget(0, 0, competitorsPanel);
        grid.setWidget(0, 1, boatsPanel);
        grid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        mainPanel.add(grid);
        competitorTable.refreshCompetitorListFromRace(leaderboardName, raceColumnName, fleetName);
        boatTable.refreshBoatListFromRace(leaderboardName, raceColumnName, fleetName);
    }

    private void competitorSelectionChanged() {
        CompetitorDTO selectedCompetitor = getSelectedCompetitor();
        if (selectedCompetitor != null) {
            selectBoatForCompetitor(selectedCompetitor);
        } else {
            boatTable.getSelectionModel().clear();
        }
    }

    private void selectBoatInList(BoatDTO boat) {
        boatTable.selectBoat(boat);
    }

    private void selectBoatForCompetitor(CompetitorDTO selectedCompetitor) {
        sailingService.getBoatLinkedToCompetitorForRace(leaderboardName,
                raceColumnName, fleetName, selectedCompetitor.getIdAsString(), new MarkedAsyncCallback<BoatDTO>(
                        new AsyncCallback<BoatDTO>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError(stringMessages.errorTryingToDetermineBoatLinkedToCompetitor(raceColumnName, leaderboardName, t.getMessage()));
                            }
                            
                            @Override
                            public void onSuccess(BoatDTO boat) {
                                // This method should select the linked boat.
                                // So you don't want to link or unlink it again throw the trackedRaceListHandler.
                                if (boat != null) {
                                    selectBoatInList(boat);
                                } else {
                                    boatTable.clearSelection();
                                }
                            }
                        }));
    }

    private CompetitorDTO getSelectedCompetitor() {
        if (competitorTable.getSelectionModel().getSelectedSet().isEmpty()) {
            return null;
        }
        return competitorTable.getSelectionModel().getSelectedSet().iterator().next();
    }
}
