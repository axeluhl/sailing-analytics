package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.controls.busyindicator.BusyDisplay;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

/**
 * Allows an administrator to view and edit boats assigned to the competitors of a race.
 * 
 * @author Frank Mittag
 * 
 */
public class CompetitorToBoatMappingsPanel extends SimplePanel implements BusyDisplay {
    private final CompactCompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;
    private final CompactBoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> boatTable;
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    
    private final RefreshableSelectionModel<BoatDTO> refreshableBoatSelectionModel;
    private final RefreshableSelectionModel<CompetitorDTO> refreshableCompetitorSelectionModel;

    protected final SelectionChangeEvent.Handler boatListHandler;
    protected HandlerRegistration boatListHandlerRegistration;

    private final BusyIndicator busyIndicator;

    public CompetitorToBoatMappingsPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName, final String raceColumnName, final String fleetName,
            boolean enableChangeActions) {
        super();
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.errorReporter = errorReporter;

        this.competitorTable = new CompactCompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ false, /* enablePager */ true);
        this.boatTable = new CompactBoatTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ false, /* enablePager */ true);

        ImagesBarColumn<CompetitorDTO, CompactCompetitorConfigImagesBarCell> raceActionColumn = new ImagesBarColumn<>(new CompactCompetitorConfigImagesBarCell(stringMessages));
        raceActionColumn.setFieldUpdater(new FieldUpdater<CompetitorDTO, String>() {
            @Override
            public void update(int index, CompetitorDTO competitor, String value) {
                if (CompactCompetitorConfigImagesBarCell.ACTION_UNLINK.equals(value)) {
                    unlinkBoatFromCompetitor(competitor);
                }
            }
        });
        if (enableChangeActions) {
            competitorTable.getTable().addColumn(raceActionColumn, stringMessages.actions());
        }
        
        refreshableBoatSelectionModel = boatTable.getSelectionModel();
        refreshableCompetitorSelectionModel = competitorTable.getSelectionModel();

        boatListHandler = new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<BoatDTO> selectedBoats = refreshableBoatSelectionModel.getSelectedSet();
                CompetitorDTO selectedCompetitor = getSelectedCompetitor();
                // if no competitor is selected, ignore the boat selection change
                if (selectedCompetitor != null) {
                    if (selectedBoats.isEmpty()) {
                        if (hasLink(selectedCompetitor)) {
                            unlinkBoatFromCompetitor(selectedCompetitor);
                        }
                    } else {
                        BoatDTO selectedBoat = selectedBoats.iterator().next();
                        if (hasLink(selectedCompetitor) && !isLinkedToBoat(selectedCompetitor, selectedBoat)) {
                            if (Window.confirm(stringMessages.boatAlreadyLinked())) {
                                linkBoatToSelectedCompetitor(selectedCompetitor, selectedBoat);
                            } else {
                                selectBoatForCompetitor(selectedCompetitor);
                            }
                        } else {
                            linkBoatToSelectedCompetitor(selectedCompetitor, selectedBoat);
                        }
                    }
                }
            }

            private boolean hasLink(CompetitorDTO selectedCompetitor) {
                return selectedCompetitor.getBoat() != null;
            }
            
            private boolean isLinkedToBoat(CompetitorDTO selectedCompetitor, BoatDTO selectedBoat){
                return selectedBoat.equals(selectedCompetitor.getBoat());
            }
        };
        if (enableChangeActions) {
            // boat selection changes should only have an effect in case the 'actions' are enabled
            boatListHandlerRegistration = refreshableBoatSelectionModel.addSelectionChangeHandler(boatListHandler);
        }

        refreshableCompetitorSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                // If the selection on the competitorTable changes,
                // you don't want to link or unlink competitors with the
                // boatListHandler.
                removeBoatListHandlerTemporarily();
                competitorSelectionChanged();
            }
        });

        busyIndicator = new SimpleBusyIndicator(false, 0.8f);

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
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshData();
            }
        });
        refreshButton.ensureDebugId("RefreshButton");
        buttonPanel.add(refreshButton);
        
        mainPanel.add(buttonPanel);
        mainPanel.add(busyIndicator);
        
        Grid grid = new Grid(1,2);
        grid.setWidget(0, 0, competitorsPanel);
        grid.setWidget(0, 1, boatsPanel);
        grid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        mainPanel.add(grid);
        
        refreshData();
    }

    private void linkBoatToSelectedCompetitor(CompetitorDTO selectedCompetitor, BoatDTO selectedBoat) {
        sailingService.linkBoatToCompetitorForRace(leaderboardName, raceColumnName, fleetName, selectedCompetitor.getIdAsString(), selectedBoat.getIdAsString(),
                new MarkedAsyncCallback<Boolean>(
                        new AsyncCallback<Boolean>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to link boat " + selectedBoat.getIdAsString() + " with competitor " + 
                                        selectedCompetitor.getIdAsString() + " for raceColumn " + raceColumnName + " and fleet " + fleetName + 
                                        " of leaderboard " + leaderboardName + ": " + t.getMessage());
                                boatTable.getSelectionModel().clear();
                            }
                
                            @Override
                            public void onSuccess(Boolean success) {
                                if (success) {
                                    selectedCompetitor.setBoat(selectedBoat);
                                    competitorTable.getDataProvider().refresh();
                                } 
                            }
                        }));
    }

    private void unlinkBoatFromCompetitor(CompetitorDTO selectedCompetitor) {
        sailingService.unlinkBoatFromCompetitorForRace(leaderboardName, raceColumnName, fleetName, selectedCompetitor.getIdAsString(),
                new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to unlink boat from competitor " + 
                        selectedCompetitor.getIdAsString() + " for raceColumn " + raceColumnName + " and fleet " + fleetName + 
                        " of leaderboard " + leaderboardName + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    boatTable.getSelectionModel().clear();
                    getSelectedCompetitor().setBoat(null);
                    competitorTable.getDataProvider().refresh();
                }
            }
        });
    }

    private void competitorSelectionChanged() {
        CompetitorDTO selectedCompetitor = getSelectedCompetitor();
        if (selectedCompetitor != null) {
            selectBoatForCompetitor(selectedCompetitor);
        } else {
            boatTable.getSelectionModel().clear();;
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
                                errorReporter.reportError("Error trying to determine boat linked to competitor "
                                        + raceColumnName + " in leaderboard " + leaderboardName + ": "
                                        + t.getMessage());
                            }
                            
                            @Override
                            public void onSuccess(BoatDTO boat) {
                                // This method should select the linked boat.
                                // So you don't want to link or unlink it again throw the trackedRaceListHandler.
                                removeBoatListHandlerTemporarily();
                                if (boat != null) {
                                    selectBoatInList(boat);
                                } else {
                                    boatTable.clearSelection();
                                }
                            }
                        }));
    }

    /**
     * Removes the {@link SelectionChangeEvent.Handler} until the browser regains control. The handler will be added
     * again using {@link Scheduler#scheduleDeferred(ScheduledCommand)} method.
     * <p>
     * Use this method if you change the {@link ListDataProvider} or {@link RefreshableSelectionModel} of
     * {@link BoatTableWrapper} and you don't want to trigger the
     * {@link SelectionChangeEvent.Handler#onSelectionChange(SelectionChangeEvent)}.
     */
    private void removeBoatListHandlerTemporarily() {
        if (boatListHandlerRegistration == null) {
            return;
        }
        boatListHandlerRegistration.removeHandler();
        boatListHandlerRegistration = null;
        // It is necessary to do this with the ScheduleDeferred() method,
        // because the SelectionChangeEvent isn't fired directly after
        // selection changes. So an remove of SelectionChangeHandler before 
        // the selection change and and new registration directly after it
        // isn't possible.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                boatListHandlerRegistration = refreshableBoatSelectionModel.addSelectionChangeHandler(boatListHandler);
            }
        });
    }

    private CompetitorDTO getSelectedCompetitor() {
        if (competitorTable.getSelectionModel().getSelectedSet().isEmpty()) {
            return null;
        }
        return competitorTable.getSelectionModel().getSelectedSet().iterator().next();
    }

    public void refreshData() {
        competitorTable.refreshCompetitorList(leaderboardName, raceColumnName, fleetName);
        boatTable.refreshBoatList();
    }

    @Override
    public void setBusy(boolean isBusy) {
        if (busyIndicator.isBusy() != isBusy) {
            busyIndicator.setBusy(isBusy);
        }
    }
}
