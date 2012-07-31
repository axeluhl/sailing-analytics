package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BuoyDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.RaceBuoysDTO;

/**
 * A panel that has a race selection (inherited from {@link AbstractRaceManagementPanel}) and which adds a table
 * for a selected race showing the race's waypoints together with the number of mark passings already received for that
 * waypoint. Also, the control can be used to send course updates into the tracked race, mostly to simulate these types
 * of events. Conceivably, this may in the future also become a way to set up and edit courses for a tracked race.
 * 
 * @author Frank Mittag (C5163874), Axel Uhl (D043530)
 */
public class RaceCourseManagementPanel extends AbstractRaceManagementPanel {
    /**
     * Represents one buoy assignment for a control point. Gates have multiple such records, one for each of their buoys.
     * 
     * @author Axel Uhl (D043530)
     */
    private static class ControlPointAndOldAndNewBuoy {
        private final ControlPointDTO controlPoint;
        private final BuoyDTO oldBuoy;
        private BuoyDTO newBuoy;
        public ControlPointAndOldAndNewBuoy(ControlPointDTO controlPoint, BuoyDTO oldBuoy) {
            super();
            this.controlPoint = controlPoint;
            this.oldBuoy = oldBuoy;
            this.newBuoy = oldBuoy;
        }
        public BuoyDTO getNewBuoy() {
            return newBuoy;
        }
        public void setNewBuoy(BuoyDTO newBuoy) {
            this.newBuoy = newBuoy;
        }
        public ControlPointDTO getControlPoint() {
            return controlPoint;
        }
        public BuoyDTO getOldBuoy() {
            return oldBuoy;
        }
    }
    
    private class ControlPointCreationDialog extends DataEntryDialog<ControlPointDTO> {
        private final CellTable<BuoyDTO> buoysTable;
        private final MultiSelectionModel<BuoyDTO> selectionModel;
        
        public ControlPointCreationDialog(final StringMessages stringMessages, AdminConsoleTableResources tableRes,
                AsyncCallback<ControlPointDTO> callback) {
            super(stringMessages.controlPoint(), stringMessages.selectOneBuoyOrTwoBuoysForGate(),
                    stringMessages.ok(), stringMessages.cancel(), new Validator<ControlPointDTO>() {
                        @Override
                        public String getErrorMessage(ControlPointDTO valueToValidate) {
                            if (valueToValidate == null) {
                                return stringMessages.selectOneBuoyOrTwoBuoysForGate();
                            } else {
                                return null;
                            }
                        }

                    }, callback);
            selectionModel = new MultiSelectionModel<BuoyDTO>();
            buoysTable = createBuoysTable(stringMessages, tableRes, selectionModel);
        }

        @Override
        protected ControlPointDTO getResult() {
            ControlPointDTO result = null;
            Set<BuoyDTO> selection = selectionModel.getSelectedSet();
            if (selection.size() == 1) {
                result = selectionModel.getSelectedSet().iterator().next();
            } else if (selection.size() == 2) {
                Iterator<BuoyDTO> i = selectionModel.getSelectedSet().iterator();
                BuoyDTO first = i.next();
                BuoyDTO second = i.next();
                BuoyDTO left;
                BuoyDTO right;
                String gateName;
                if (first.name.toLowerCase().contains("left")) {
                    left = first;
                    right = second;
                } else {
                    left = second;
                    right = first;
                }
                gateName = left.name.replaceFirst("( \\()?[lL][eE][fF][tT]\\)?", "");
                result = new GateDTO(gateName, left, right);
            }
            return result;
        }

        @Override
        protected Widget getAdditionalWidget() {
            return buoysTable;
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
    private final CellTable<ControlPointAndOldAndNewBuoy> controlPointsTable;
    private final MultiSelectionModel<ControlPointAndOldAndNewBuoy> controlPointsSelectionModel; 
    private final ListDataProvider<ControlPointAndOldAndNewBuoy> controlPointDataProvider;

    private final HorizontalPanel courseActionsPanel;
    
    private final Handler buoySelectionChangeHandler;
    private final Button insertWaypointBefore;
    private final Button insertWaypointAfter;
    private final Button removeWaypointBtn;
    private boolean ignoreWaypointAndOldAndNewBuoySelectionChange;
    
    public RaceCourseManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, final StringMessages stringMessages) {
        super(sailingService, errorReporter, regattaRefresher, stringMessages);
        Grid grid = new Grid(1, 2);
        grid.setCellPadding(10);
        selectedRaceContentPanel.add(grid);
        
        final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        controlPointsTable = new CellTable<ControlPointAndOldAndNewBuoy>(/* pageSize */10000, tableRes);
        grid.setWidget(0,  0, controlPointsTable);
        controlPointsSelectionModel = new MultiSelectionModel<ControlPointAndOldAndNewBuoy>();
        controlPointsTable.setSelectionModel(controlPointsSelectionModel);
        controlPointsSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                ignoreWaypointAndOldAndNewBuoySelectionChange = true;
                try {
                    buoySelectionModel.setSelected(buoySelectionModel.getSelectedObject(), false);
                    final int selectionSize = controlPointsSelectionModel.getSelectedSet().size();
                    insertWaypointAfter.setEnabled(selectionSize==1);
                    insertWaypointBefore.setEnabled(selectionSize==1);
                    removeWaypointBtn.setEnabled(selectionSize>=1);
                    if (selectionSize == 1) {
                        BuoyDTO newBuoy = controlPointsSelectionModel.getSelectedSet().iterator().next().getNewBuoy();
                        if (newBuoy != null) {
                            buoySelectionModel.setSelected(newBuoy, true);
                        }
                    }
                } finally {
                    ignoreWaypointAndOldAndNewBuoySelectionChange = false;
                }
            }
        });
        TextColumn<ControlPointAndOldAndNewBuoy> nameColumn = new TextColumn<ControlPointAndOldAndNewBuoy>() {
            @Override
            public String getValue(ControlPointAndOldAndNewBuoy cpaoanb) {
                return cpaoanb.getControlPoint().name;
            }
        }; 
        controlPointsTable.addColumn(nameColumn, stringMessages.controlPoint());
        TextColumn<ControlPointAndOldAndNewBuoy> oldBuoyColumn = new TextColumn<ControlPointAndOldAndNewBuoy>() {
            @Override
            public String getValue(ControlPointAndOldAndNewBuoy cpaoanb) {
                return "" + cpaoanb.getOldBuoy().name;
            }
        }; 
        controlPointsTable.addColumn(oldBuoyColumn, stringMessages.buoy());
        TextColumn<ControlPointAndOldAndNewBuoy> newBuoyColumn = new TextColumn<ControlPointAndOldAndNewBuoy>() {
            @Override
            public String getValue(ControlPointAndOldAndNewBuoy cpaoanb) {
                return "" + cpaoanb.getNewBuoy().name;
            }
        }; 
        controlPointsTable.addColumn(newBuoyColumn, stringMessages.newBuoy());
        controlPointDataProvider = new ListDataProvider<ControlPointAndOldAndNewBuoy>();
        controlPointDataProvider.addDataDisplay(controlPointsTable);

        // race buoys table
        buoyDataProvider = new ListDataProvider<BuoyDTO>();
        buoySelectionChangeHandler = new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!ignoreWaypointAndOldAndNewBuoySelectionChange) {
                    updateNewBuoy(controlPointsSelectionModel.getSelectedSet(), buoySelectionModel.getSelectedObject());
                }
            }
        };
        buoySelectionModel = new SingleSelectionModel<BuoyDTO>();
        buoysTable = createBuoysTable(stringMessages, tableRes, buoySelectionModel);
        buoysTable.getSelectionModel().addSelectionChangeHandler(buoySelectionChangeHandler);
        buoyDataProvider.addDataDisplay(buoysTable);
        grid.setWidget(0,  1, buoysTable);
        
        courseActionsPanel = new HorizontalPanel();
        courseActionsPanel.setSpacing(10);
        insertWaypointBefore = new Button("Insert waypoint before selected"); // TODO i18n
        insertWaypointBefore.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                insertWaypoint(sailingService, stringMessages, tableRes, /* before */ true);
            }

        });
        insertWaypointBefore.setEnabled(false);
        courseActionsPanel.add(insertWaypointBefore);
        insertWaypointAfter = new Button("Insert waypoint after selected"); // TODO i18n
        insertWaypointAfter.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                insertWaypoint(sailingService, stringMessages, tableRes, /* before */ false);
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
        Button saveBtn = new Button(stringMessages.save());
        saveBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveCourse(sailingService, stringMessages);
            }
        });
        courseActionsPanel.add(saveBtn);
        courseActionsPanel.setVisible(false);
        this.selectedRaceContentPanel.add(courseActionsPanel);
    }

    private void saveCourse(SailingServiceAsync sailingService, final StringMessages stringMessages) {
        List<ControlPointDTO> controlPoints = new ArrayList<ControlPointDTO>();
        for (ControlPointAndOldAndNewBuoy cpaoanb : controlPointDataProvider.getList()) {
            if (!controlPoints.contains(cpaoanb.getControlPoint())) {
                controlPoints.add(cpaoanb.getControlPoint());
            }
        }
        sailingService.updateRaceCourse(singleSelectedRace, controlPoints, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorUpdatingRaceCourse(caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                Window.setStatus(stringMessages.successfullyUpdatedCourse());
            }
        });
    }

    private CellTable<BuoyDTO> createBuoysTable(final StringMessages stringMessages, AdminConsoleTableResources tableRes,
            SelectionModel<BuoyDTO> selectionModel) {
        CellTable<BuoyDTO> result = new CellTable<BuoyDTO>(/* pageSize */10000, tableRes);
        result.setSelectionModel(selectionModel);
        TextColumn<BuoyDTO> buoyNameColumn = new TextColumn<BuoyDTO>() {
            @Override
            public String getValue(BuoyDTO buoyDTO) {
                return buoyDTO.name;
            }
        };
        result.addColumn(buoyNameColumn, stringMessages.buoy());
        final SafeHtmlCell buoyPositionCell = new SafeHtmlCell();
        Column<BuoyDTO, SafeHtml> buoyPositionColumn = new Column<BuoyDTO, SafeHtml>(buoyPositionCell) {
            @Override
            public SafeHtml getValue(BuoyDTO buoy) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.appendEscaped(buoy.name + ", " + stringMessages.position() + ": " + buoy.position.toFormattedString());
                return builder.toSafeHtml();
            }
        };
        result.addColumn(buoyPositionColumn, stringMessages.position());
        return result;
    }

    private void updateNewBuoy(Set<ControlPointAndOldAndNewBuoy> selectedWaypointsAndOldAndNewBuoys, BuoyDTO selectedNewBuoy) {
        if (selectedWaypointsAndOldAndNewBuoys != null) {
            for (ControlPointAndOldAndNewBuoy w : selectedWaypointsAndOldAndNewBuoys) {
                if (selectedNewBuoy == null) {
                    w.setNewBuoy(w.getOldBuoy());
                } else {
                    w.setNewBuoy(selectedNewBuoy);
                }
                controlPointDataProvider.getList().set(controlPointDataProvider.getList().indexOf(w), w);
            }
        }
    }

    private void insertWaypoint(final SailingServiceAsync sailingService, StringMessages stringMessages,
            AdminConsoleTableResources tableRes, final boolean beforeSelection) {
        new ControlPointCreationDialog(stringMessages, tableRes, new AsyncCallback<ControlPointDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                // dialog cancelled, do nothing
            }

            @Override
            public void onSuccess(ControlPointDTO result) {
                Set<ControlPointAndOldAndNewBuoy> selectedElements = controlPointsSelectionModel.getSelectedSet();
                if (!selectedElements.isEmpty()) {
                    ControlPointAndOldAndNewBuoy selectedElement = selectedElements.iterator().next();
                    int insertPos = controlPointDataProvider.getList().indexOf(selectedElement) + (beforeSelection?0:1);
                    for (BuoyDTO buoyDTO : result.getBuoys()) {
                        controlPointDataProvider.getList().add(insertPos++, new ControlPointAndOldAndNewBuoy(result, buoyDTO));
                    }
                }
            }
        }).show();
    }

    @Override
    void refreshSelectedRaceData() {
        if (singleSelectedRace != null && selectedRaceDTO != null) {
            courseActionsPanel.setVisible(true);
            sailingService.getRaceCourse(singleSelectedRace, new Date(),  new AsyncCallback<List<ControlPointDTO>>() {
                @Override
                public void onSuccess(final List<ControlPointDTO> controlPoints) {
                    sailingService.getRaceBuoys(singleSelectedRace, new Date(),  new AsyncCallback<RaceBuoysDTO>() {
                        @Override
                        public void onSuccess(RaceBuoysDTO raceBuoysDTO) {
                            updateCourseAndBuoysInfo(controlPoints, raceBuoysDTO);
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            RaceCourseManagementPanel.this.errorReporter.reportError("Error trying to obtain the buoys of the race: " + caught.getMessage()); // TODO i18n
                        }
                    });
                }

                @Override
                public void onFailure(Throwable caught) {
                    RaceCourseManagementPanel.this.errorReporter.reportError("Error trying to obtain the course of the race: " + caught.getMessage()); // TODO i18n
                }
            });
        } else {
            courseActionsPanel.setVisible(false);
        }
    }

    private void updateCourseAndBuoysInfo(List<ControlPointDTO> controlPoints, RaceBuoysDTO buoysDTO) {
        List<ControlPointAndOldAndNewBuoy> waypointsAndOldAndNewBuoys = new ArrayList<ControlPointAndOldAndNewBuoy>();
        for (ControlPointDTO controlPointDTO : controlPoints) {
            for (BuoyDTO buoy : controlPointDTO.getBuoys()) {
                ControlPointAndOldAndNewBuoy waypointAndOldAndNewBuoy = new ControlPointAndOldAndNewBuoy(controlPointDTO, buoy);
                waypointsAndOldAndNewBuoys.add(waypointAndOldAndNewBuoy);
            }
        }
        controlPointDataProvider.getList().clear();
        controlPointDataProvider.getList().addAll(waypointsAndOldAndNewBuoys);
        buoyDataProvider.setList(new ArrayList<BuoyDTO>(buoysDTO.buoys));
        for (ControlPointAndOldAndNewBuoy w : controlPointsSelectionModel.getSelectedSet()) {
            controlPointsSelectionModel.setSelected(w, false);
        }
    }

    private void removeSelectedWaypoints(final SailingServiceAsync sailingService) {
        final Set<ControlPointDTO> selectedControlPoints = new HashSet<ControlPointDTO>();
        for (ControlPointAndOldAndNewBuoy cpaoanb : controlPointsSelectionModel.getSelectedSet()) {
            selectedControlPoints.add(cpaoanb.getControlPoint());
        }
        for (Iterator<ControlPointAndOldAndNewBuoy> i=controlPointDataProvider.getList().iterator(); i.hasNext(); ) {
            ControlPointAndOldAndNewBuoy next = i.next();
            if (selectedControlPoints.contains(next.getControlPoint())) {
                i.remove();
            }
        }
    }
}
