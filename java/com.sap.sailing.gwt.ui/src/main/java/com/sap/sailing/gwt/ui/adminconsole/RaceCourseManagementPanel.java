package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BuoyDTO;
import com.sap.sailing.gwt.ui.shared.RaceBuoysDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;

/**
 * A panel that has a race selection (inherited from {@link AbstractRaceManagementPanel}) and which adds a table
 * for a selected race showing the race's waypoints together with the number of mark passings already received for that
 * waypoint. Also, the control can be used to send course updates into the tracked race, mostly to simulate these types
 * of events. Conceivably, this may in the future also become a way to set up and edit courses for a tracked race.
 * 
 * @author Frank Mittag (C5163874), Axel Uhl (D043530)
 */
public class RaceCourseManagementPanel extends AbstractRaceManagementPanel {
    private static class WaypointAndOldAndNewBuoy {
        private final WaypointDTO waypoint;
        private final BuoyDTO oldBuoy;
        private BuoyDTO newBuoy;
        public WaypointAndOldAndNewBuoy(WaypointDTO waypoint, BuoyDTO oldBuoy) {
            super();
            this.waypoint = waypoint;
            this.oldBuoy = oldBuoy;
            this.newBuoy = oldBuoy;
        }
        public BuoyDTO getNewBuoy() {
            return newBuoy;
        }
        public void setNewBuoy(BuoyDTO newBuoy) {
            this.newBuoy = newBuoy;
        }
        public WaypointDTO getWaypoint() {
            return waypoint;
        }
        public BuoyDTO getOldBuoy() {
            return oldBuoy;
        }
    }

    /**
     * A table that lists the buoys for which events have been received for the race selected. Note that this list may
     * be longer than the list of buoys actually used by the control points backing the course's waypoints because of
     * the possibility of spare marks / buoys.
     */
    private final CellTable<BuoyDTO> buoysTable;
    private final ListDataProvider<BuoyDTO> buoyDataProvider;
    private final SingleSelectionModel<BuoyDTO> buoySelectionModel;

    /**
     * A table that lists the product of Waypoint x ControlPoint x Buoy plus a hint as to the number of mark passings.
     * The (multi-)selection on this table can be used as either a selection of waypoints or a selection of control points
     * or a selection of buoys.
     */
    private final CellTable<WaypointAndOldAndNewBuoy> courseWaypointsTable;
    private final MultiSelectionModel<WaypointAndOldAndNewBuoy> wayPointSelectionModel; 
    private final ListDataProvider<WaypointAndOldAndNewBuoy> waypointDataProvider;

    private final Label courseDataRequestTimeLabel;
    
    private final HorizontalPanel courseActionsPanel;
    
    private final Handler buoySelectionChangeHandler;
    private final Button insertWaypointBefore;
    private final Button insertWaypointAfter;
    private final Button removeWaypointBtn;
    private boolean ignoreWaypointAndOldAndNewBuoySelectionChange;
    
    public RaceCourseManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, final StringMessages stringMessages) {
        super(sailingService, errorReporter, regattaRefresher, stringMessages);
        courseDataRequestTimeLabel = new Label();
        selectedRaceContentPanel.add(courseDataRequestTimeLabel);
        Grid grid = new Grid(1, 2);
        grid.setCellPadding(10);
        selectedRaceContentPanel.add(grid);
        
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        courseWaypointsTable = new CellTable<WaypointAndOldAndNewBuoy>(/* pageSize */10000, tableRes);
        grid.setWidget(0,  0, courseWaypointsTable);
        wayPointSelectionModel = new MultiSelectionModel<WaypointAndOldAndNewBuoy>();
        courseWaypointsTable.setSelectionModel(wayPointSelectionModel);
        wayPointSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                ignoreWaypointAndOldAndNewBuoySelectionChange = true;
                try {
                    buoySelectionModel.setSelected(buoySelectionModel.getSelectedObject(), false);
                    final int selectionSize = wayPointSelectionModel.getSelectedSet().size();
                    insertWaypointAfter.setEnabled(selectionSize==1);
                    insertWaypointBefore.setEnabled(selectionSize==1);
                    removeWaypointBtn.setEnabled(selectionSize>=1);
                    if (selectionSize == 1) {
                        BuoyDTO newBuoy = wayPointSelectionModel.getSelectedSet().iterator().next().getNewBuoy();
                        if (newBuoy != null) {
                            buoySelectionModel.setSelected(newBuoy, true);
                        }
                    }
                } finally {
                    ignoreWaypointAndOldAndNewBuoySelectionChange = false;
                }
            }
        });
        TextColumn<WaypointAndOldAndNewBuoy> nameColumn = new TextColumn<WaypointAndOldAndNewBuoy>() {
            @Override
            public String getValue(WaypointAndOldAndNewBuoy waypointDTO) {
                return waypointDTO.getWaypoint().name;
            }
        }; 
        courseWaypointsTable.addColumn(nameColumn, stringMessages.waypoint());
        TextColumn<WaypointAndOldAndNewBuoy> markPassingsCountColumn = new TextColumn<WaypointAndOldAndNewBuoy>() {
            @Override
            public String getValue(WaypointAndOldAndNewBuoy waypointDTO) {
                return "" + waypointDTO.getWaypoint().markPassingsCount;
            }
        }; 
        courseWaypointsTable.addColumn(markPassingsCountColumn, stringMessages.markPassing());
        TextColumn<WaypointAndOldAndNewBuoy> oldBuoyColumn = new TextColumn<WaypointAndOldAndNewBuoy>() {
            @Override
            public String getValue(WaypointAndOldAndNewBuoy waypointDTO) {
                return "" + waypointDTO.getOldBuoy().name;
            }
        }; 
        courseWaypointsTable.addColumn(oldBuoyColumn, stringMessages.buoy());
        TextColumn<WaypointAndOldAndNewBuoy> newBuoyColumn = new TextColumn<WaypointAndOldAndNewBuoy>() {
            @Override
            public String getValue(WaypointAndOldAndNewBuoy waypointDTO) {
                return "" + waypointDTO.getNewBuoy().name;
            }
        }; 
        courseWaypointsTable.addColumn(newBuoyColumn, stringMessages.newBuoy());
        waypointDataProvider = new ListDataProvider<WaypointAndOldAndNewBuoy>();
        waypointDataProvider.addDataDisplay(courseWaypointsTable);

        // race buoys table
        buoysTable = new CellTable<BuoyDTO>(/* pageSize */10000, tableRes);
        grid.setWidget(0,  1, buoysTable);
        buoySelectionModel = new SingleSelectionModel<BuoyDTO>();
        buoysTable.setSelectionModel(buoySelectionModel);
        buoySelectionChangeHandler = new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!ignoreWaypointAndOldAndNewBuoySelectionChange) {
                    updateNewBuoy(wayPointSelectionModel.getSelectedSet(), buoySelectionModel.getSelectedObject());
                }
            }
        };
        buoySelectionModel.addSelectionChangeHandler(buoySelectionChangeHandler);
        TextColumn<BuoyDTO> buoyNameColumn = new TextColumn<BuoyDTO>() {
            @Override
            public String getValue(BuoyDTO markDTO) {
                return markDTO.name;
            }
        }; 
        buoysTable.addColumn(buoyNameColumn, stringMessages.buoy());
        final SafeHtmlCell buoyPositionCell = new SafeHtmlCell();
        Column<BuoyDTO, SafeHtml> buoyPositionColumn = new Column<BuoyDTO, SafeHtml>(buoyPositionCell) {
            @Override
            public SafeHtml getValue(BuoyDTO mark) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.appendEscaped(mark.name + ", " + stringMessages.position() + ": " + mark.position.toFormattedString());
                return builder.toSafeHtml();
            }
        };
        buoysTable.addColumn(buoyPositionColumn, stringMessages.position());
        buoyDataProvider = new ListDataProvider<BuoyDTO>();
        buoyDataProvider.addDataDisplay(buoysTable);
        
        courseActionsPanel = new HorizontalPanel();
        courseActionsPanel.setSpacing(10);
        insertWaypointBefore = new Button("Insert waypoint before selected");
        insertWaypointBefore.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                insertWaypoint(sailingService, /* before */ true);
            }

        });
        insertWaypointBefore.setEnabled(false);
        courseActionsPanel.add(insertWaypointBefore);
        insertWaypointAfter = new Button("Insert waypoint after selected");
        insertWaypointAfter.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                insertWaypoint(sailingService, /* before */ false);
            }

        });
        insertWaypointAfter.setEnabled(false);
        courseActionsPanel.add(insertWaypointAfter);
        removeWaypointBtn = new Button(stringMessages.remove());
        removeWaypointBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedWaypoints(sailingService);
            }
        });
        removeWaypointBtn.setEnabled(false);
        courseActionsPanel.add(removeWaypointBtn);
        Button refreshBtn = new Button(stringMessages.refresh());
        refreshBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshSelectedRaceData();
            }
        });
        courseActionsPanel.add(refreshBtn);
        courseActionsPanel.setVisible(false);
        this.selectedRaceContentPanel.add(courseActionsPanel);
    }

    private void updateNewBuoy(Set<WaypointAndOldAndNewBuoy> selectedWaypointsAndOldAndNewBuoys, BuoyDTO selectedNewBuoy) {
        if (selectedWaypointsAndOldAndNewBuoys != null) {
            for (WaypointAndOldAndNewBuoy w : selectedWaypointsAndOldAndNewBuoys) {
                if (selectedNewBuoy == null) {
                    w.setNewBuoy(w.getOldBuoy());
                } else {
                    w.setNewBuoy(selectedNewBuoy);
                }
                waypointDataProvider.getList().set(waypointDataProvider.getList().indexOf(w), w);
            }
        }
    }

    private void insertWaypoint(final SailingServiceAsync sailingService, boolean beforeSelection) {
        // TODO present a popup that shows the buoy table again and lets the user select one or two buoys for a BuoyDTO or GateDTO
        // TODO select insert position based on after/before and current waypoint selection
        if(wayPointSelectionModel.getSelectedSet().size() != 2) {
            Window.alert("To insert (copy) a round you have to select two waypoints.");
            return;
        } else {
            Map<Integer, String> namesOfControlPointsToInsert = new TreeMap<Integer, String>();
            for (WaypointAndOldAndNewBuoy selectedWaypoint: wayPointSelectionModel.getSelectedSet()) {
                namesOfControlPointsToInsert.put(selectedWaypoint.getWaypoint().courseIndex, selectedWaypoint.getWaypoint().name);
            }
        }
    }

    @Override
    void refreshSelectedRaceData() {
        if (singleSelectedRace != null && selectedRaceDTO != null) {
            courseActionsPanel.setVisible(true);
            sailingService.getRaceCourse(singleSelectedRace, new Date(),  new AsyncCallback<RaceCourseDTO>() {
                @Override
                public void onSuccess(final RaceCourseDTO raceCourseDTO) {
                    sailingService.getRaceBuoys(singleSelectedRace, new Date(),  new AsyncCallback<RaceBuoysDTO>() {
                        @Override
                        public void onSuccess(RaceBuoysDTO raceBuoysDTO) {
                            updateCourseAndBuoysInfo(raceCourseDTO, raceBuoysDTO);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            RaceCourseManagementPanel.this.errorReporter.reportError("Error trying to obtain the buoys of the race: " + caught.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Throwable caught) {
                    RaceCourseManagementPanel.this.errorReporter.reportError("Error trying to obtain the course of the race: " + caught.getMessage());
                }
            });
        } else {
            courseActionsPanel.setVisible(false);
        }
    }

    private void updateCourseAndBuoysInfo(RaceCourseDTO courseDTO, RaceBuoysDTO buoysDTO) {
        List<WaypointAndOldAndNewBuoy> waypointsAndOldAndNewBuoys = new ArrayList<WaypointAndOldAndNewBuoy>();
        for (WaypointDTO waypoint : courseDTO.waypoints) {
            for (BuoyDTO buoy : waypoint.buoys) {
                WaypointAndOldAndNewBuoy waypointAndOldAndNewBuoy = new WaypointAndOldAndNewBuoy(waypoint, buoy);
                waypointsAndOldAndNewBuoys.add(waypointAndOldAndNewBuoy);
            }
        }
        waypointDataProvider.setList(waypointsAndOldAndNewBuoys);
        buoyDataProvider.setList(new ArrayList<BuoyDTO>(buoysDTO.buoys));
        courseDataRequestTimeLabel.setText(courseDTO.requestTime.toString());
        for (WaypointAndOldAndNewBuoy w : wayPointSelectionModel.getSelectedSet()) {
            wayPointSelectionModel.setSelected(w, false);
        }
    }

    private void removeSelectedWaypoints(final SailingServiceAsync sailingService) {
        final Set<WaypointAndOldAndNewBuoy> selectedSet = new HashSet<WaypointAndOldAndNewBuoy>(wayPointSelectionModel.getSelectedSet());
        for (WaypointAndOldAndNewBuoy selectedWaypoint : selectedSet) {
            waypointDataProvider.getList().remove(selectedWaypoint);
        }
    }
}
