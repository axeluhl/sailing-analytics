 package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Allows an administrator to map a list of given competitors to boats for a race.
 * 
 * @author Frank Mittag
 * 
 */
public class CompetitorToBoatMappingsDialog extends DataEntryDialog<Map<CompetitorDTO, BoatDTO>> {
    private final CompactCompetitorTableWrapper<RefreshableSingleSelectionModel<CompetitorDTO>> competitorTable;
    private final CompactBoatTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> boatTable;
    private final RefreshableSelectionModel<BoatDTO> refreshableBoatSelectionModel;
    private final RefreshableSelectionModel<CompetitorDTO> refreshableCompetitorSelectionModel;
    private final StringMessages stringMessages;
    private final SelectionChangeEvent.Handler boatListHandler;
    private HandlerRegistration boatListHandlerRegistration;
    private final Map<CompetitorDTO, BoatDTO> competitorToBoatMappings; 
    
    protected static class CompetitorToBoatMappingValidator implements Validator<Map<CompetitorDTO, BoatDTO>> {        
        @Override
        public String getErrorMessage(Map<CompetitorDTO, BoatDTO> valueToValidate) {
            String errorMessage = null;
            for (Map.Entry<CompetitorDTO, BoatDTO> competitorAndBoatEntry : valueToValidate.entrySet()) {
                if (competitorAndBoatEntry.getValue() == null) {
                    errorMessage = "It's required that all competitors have an assigned boat.";
                    break;
                }
            }
            return errorMessage;
        }
    }
    
    public CompetitorToBoatMappingsDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, String leaderboardName, Map<CompetitorDTO, BoatDTO> competitorsAndBoats, DialogCallback<Map<CompetitorDTO, BoatDTO>> callback) {
        super(stringMessages.actionEditCompetitorToBoatAssignments(), null, stringMessages.ok(), stringMessages.cancel(), new CompetitorToBoatMappingValidator(), callback);
        this.stringMessages = stringMessages;
        this.competitorToBoatMappings = new HashMap<>(competitorsAndBoats);
        this.competitorTable = new CompactCompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ false, /* enablePager */ true);
        this.boatTable = new CompactBoatTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ false, /* enablePager */ true);
        ImagesBarColumn<CompetitorDTO, CompactCompetitorConfigImagesBarCell> competitorActionColumn = new ImagesBarColumn<>(new CompactCompetitorConfigImagesBarCell(stringMessages));
        competitorActionColumn.setFieldUpdater(new FieldUpdater<CompetitorDTO, String>() {
            @Override
            public void update(int index, CompetitorDTO competitor, String value) {
                if (CompactCompetitorConfigImagesBarCell.ACTION_UNLINK.equals(value)) {
                    unlinkBoatFromCompetitor(competitor);
                    validateAndUpdate();
                }
            }
        });
        competitorTable.getTable().addColumn(competitorActionColumn, stringMessages.actions());
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
                        if (hasLinkedBoat(selectedCompetitor)) {
                            unlinkBoatFromCompetitor(selectedCompetitor);
                        }
                    } else {
                        BoatDTO selectedBoat = selectedBoats.iterator().next();
                        if (hasLinkedBoat(selectedCompetitor)) {
                            // competitor has already a linked but, but now another one is selected
                            if (!isLinkedToBoat(selectedCompetitor, selectedBoat) && !isBoatUsed(selectedBoat)) {
                                linkBoatToSelectedCompetitor(selectedCompetitor, selectedBoat);
                            } else {
                                Notification.notify(stringMessages.boatAlreadyLinked(), NotificationType.ERROR);
                            }
                        } else {
                            // check if boat is already used with another competitor
                            if (isBoatUsed(selectedBoat)) {
                                Notification.notify(stringMessages.boatAlreadyLinked(), NotificationType.ERROR);
                            } else {
                                linkBoatToSelectedCompetitor(selectedCompetitor, selectedBoat);
                            }
                        }
                    }
                }
                validateAndUpdate();
            }

            private boolean hasLinkedBoat(CompetitorDTO selectedCompetitor) {
                return competitorToBoatMappings.get(selectedCompetitor) != null;
            }
            
            private boolean isLinkedToBoat(CompetitorDTO selectedCompetitor, BoatDTO selectedBoat){
                return Util.equalsWithNull(selectedBoat, competitorToBoatMappings.get(selectedCompetitor));
            }
            
            private boolean isBoatUsed(BoatDTO selectedBoat){
                boolean boatAlreadyUsed = false;
                for (BoatDTO boat: competitorToBoatMappings.values()) {
                    if (boat != null && boat.equals(selectedBoat)) {
                        boatAlreadyUsed = true;
                        break;
                    }
                }
                return boatAlreadyUsed;
            }
        };
        // boat selection changes should only have an effect in case the 'actions' are enabled
        boatListHandlerRegistration = refreshableBoatSelectionModel.addSelectionChangeHandler(boatListHandler);
        refreshableCompetitorSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                // If the selection on the competitorTable changes,
                // you don't want to link or unlink competitors with the
                // boatListHandler.
                removeBoatListHandlerTemporarily();
                competitorSelectionChanged();
            }
        });
        competitorTable.refreshCompetitorList(competitorToBoatMappings);
        boatTable.refreshBoatList(leaderboardName);
    }

    private void linkBoatToSelectedCompetitor(CompetitorDTO selectedCompetitor, BoatDTO selectedBoat) {
        competitorToBoatMappings.put(selectedCompetitor, selectedBoat);
        competitorTable.refreshCompetitorList(competitorToBoatMappings);
        competitorTable.getDataProvider().refresh();
        competitorTable.getTable().redraw();
    }

    private void unlinkBoatFromCompetitor(CompetitorDTO selectedCompetitor) {
        competitorToBoatMappings.remove(selectedCompetitor);
        competitorToBoatMappings.put(selectedCompetitor, null);
        competitorTable.refreshCompetitorList(competitorToBoatMappings);
        boatTable.getSelectionModel().clear();
        competitorTable.getDataProvider().refresh();
        competitorTable.getTable().redraw();
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
        removeBoatListHandlerTemporarily();
        final BoatDTO boatForCompetitor = competitorToBoatMappings.get(selectedCompetitor);
        if (boatForCompetitor != null) {
            selectBoatInList(boatForCompetitor);
        } else {
            boatTable.clearSelection();
        }
        boatTable.getTable().redraw();
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

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        CaptionPanel competitorsPanel = new CaptionPanel(stringMessages.competitors());
        competitorsPanel.ensureDebugId("CompetitorsSection");
        competitorsPanel.setContentWidget(this.competitorTable.asWidget());
        CaptionPanel boatsPanel = new CaptionPanel(stringMessages.boats());
        boatsPanel.ensureDebugId("BoatsSection");
        boatsPanel.setContentWidget(this.boatTable.asWidget());
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        mainPanel.add(buttonPanel);
        Grid grid = new Grid(1,2);
        grid.setWidget(0, 0, competitorsPanel);
        grid.setWidget(0, 1, boatsPanel);
        grid.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        grid.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        mainPanel.add(grid);
        return mainPanel;
    }
        
    @Override
    protected Map<CompetitorDTO, BoatDTO> getResult() {
        return competitorToBoatMappings;
    }
}
