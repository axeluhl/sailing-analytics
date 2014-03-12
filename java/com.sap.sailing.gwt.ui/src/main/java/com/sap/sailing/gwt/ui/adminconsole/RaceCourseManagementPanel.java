package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

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
     * Represents one mark assignment for a control point. Gates have multiple such records, one for each of their marks.
     * 
     * @author Axel Uhl (D043530)
     */
    private static class ControlPointAndOldAndNewMark {
        private final ControlPointDTO controlPoint;
        private final MarkDTO oldMark;
        private MarkDTO newMark;
        private PassingInstruction passingInstructions;
        public ControlPointAndOldAndNewMark(ControlPointDTO controlPoint, PassingInstruction passingInstructions, MarkDTO oldMark) {
            super();
            this.controlPoint = controlPoint;
            this.passingInstructions = passingInstructions;
            this.oldMark = oldMark;
            this.newMark = oldMark;
        }
        public PassingInstruction getPassingInstructions() {
            return passingInstructions;
        }
        public MarkDTO getNewMark() {
            return newMark;
        }
        public void setNewMark(MarkDTO newMark) {
            this.newMark = newMark;
        }
        public ControlPointDTO getControlPoint() {
            return controlPoint;
        }
        public MarkDTO getOldMark() {
            return oldMark;
        }
    }
    
    private class ControlPointCreationDialog extends DataEntryDialog<ControlPointDTO> {
        private final CellTable<MarkDTO> marksTable;
        private final MultiSelectionModel<MarkDTO> selectionModel;
        
        public ControlPointCreationDialog(final StringMessages stringMessages, AdminConsoleTableResources tableRes,
                List<MarkDTO> marks, DialogCallback<ControlPointDTO> callback) {
            super(stringMessages.controlPoint(), stringMessages.selectOneMarkOrTwoMarksForGate(),
                    stringMessages.ok(), stringMessages.cancel(), new Validator<ControlPointDTO>() {
                        @Override
                        public String getErrorMessage(ControlPointDTO valueToValidate) {
                            if (valueToValidate == null) {
                                return stringMessages.selectOneMarkOrTwoMarksForGate();
                            } else {
                                return null;
                            }
                        }

                    }, /* animationEnabled */ false, callback);
            selectionModel = new MultiSelectionModel<MarkDTO>();
            selectionModel.addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    validate();
                }
            });
            marksTable = createAvailableMarksTable(stringMessages, tableRes, selectionModel);
            ListDataProvider<MarkDTO> markDataProvider = new ListDataProvider<MarkDTO>();
            markDataProvider.getList().addAll(marks);
            markDataProvider.addDataDisplay(marksTable);
        }

        @Override
        protected ControlPointDTO getResult() {
            ControlPointDTO result = null;
            Set<MarkDTO> selection = selectionModel.getSelectedSet();
            if (selection.size() == 1) {
                result = selectionModel.getSelectedSet().iterator().next();
            } else if (selection.size() == 2) {
                Iterator<MarkDTO> i = selectionModel.getSelectedSet().iterator();
                MarkDTO first = i.next();
                MarkDTO second = i.next();
                MarkDTO left;
                MarkDTO right;
                String gateName;
                if (first.getName().matches("^.*"+REGEX_FOR_LEFT+".*$")) {
                    left = first;
                    right = second;
                } else {
                    left = second;
                    right = first;
                }
                gateName = left.getName().replaceFirst(REGEX_FOR_LEFT, "");
                result = new GateDTO(/* generate UUID on the server */ null, gateName, left, right);
            }
            return result;
        }

        @Override
        protected Widget getAdditionalWidget() {
            return marksTable;
        }
    }
    
    private static final String REGEX_FOR_LEFT = "( \\()?(([lL][eE][fF][tT])|(1))\\)?";

    /**
     * A table that lists the marks for which events have been received for the race selected. Note that this list may
     * be longer than the list of marks actually used by the control points backing the course's waypoints because of
     * the possibility of spare marks.
     */
    private final CellTable<MarkDTO> marksTable;
    private final ListDataProvider<MarkDTO> markDataProvider;
    private final SingleSelectionModel<MarkDTO> markSelectionModel;

    /**
     * A table that lists the product of Waypoint x ControlPoint x Mark plus a hint as to the number of mark passings.
     * The (multi-)selection on this table can be used as either a selection of waypoints or a selection of control points
     * or a selection of marks.
     */
    private final CellTable<ControlPointAndOldAndNewMark> controlPointsTable;
    private final MultiSelectionModel<ControlPointAndOldAndNewMark> controlPointsSelectionModel; 
    private final ListDataProvider<ControlPointAndOldAndNewMark> controlPointDataProvider;
    
    /**
     * When for a control point's mark a replacement mark is defined (see {@link #updateNewMark(Set, MarkDTO)}),
     * the control point needs to be replaced before {@link #saveCourse(SailingServiceAsync, StringMessages) saving}.
     * Those control points are added to this set. When the mark is reset to the original mark for all the control
     * point's marks, the control point is removed from this set again. {@link #saveCourse(SailingServiceAsync, StringMessages)}
     * then is responsible for creating replacement {@link ControlPointDTO}s before sending the new control point list to the
     * server.
     */
    private final Set<ControlPointDTO> controlPointsNeedingReplacement;

    private final HorizontalPanel courseActionsPanel;
    
    private final Handler markSelectionChangeHandler;
    private final Button insertWaypointBefore;
    private final Button insertWaypointAfter;
    private final Button removeWaypointBtn;
    private boolean ignoreWaypointAndOldAndNewMarkSelectionChange;
    
    public RaceCourseManagementPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, final StringMessages stringMessages) {
        super(sailingService, errorReporter, regattaRefresher, stringMessages);
        controlPointsNeedingReplacement = new HashSet<ControlPointDTO>();
        Grid grid = new Grid(2, 2);
        grid.setCellPadding(5);
        selectedRaceContentPanel.add(grid);
        
        Label currentRaceCourseLabel = new Label("Current race course");
        currentRaceCourseLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        grid.setWidget(0, 0, currentRaceCourseLabel);
        Label availableMarksLabel = new Label("Available marks");
        availableMarksLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        grid.setWidget(0, 1, availableMarksLabel);
        grid.getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_TOP);
        
        final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        controlPointsTable = new CellTable<ControlPointAndOldAndNewMark>(/* pageSize */10000, tableRes);
        grid.setWidget(1,  0, controlPointsTable);
        controlPointsSelectionModel = new MultiSelectionModel<ControlPointAndOldAndNewMark>();
        controlPointsTable.setSelectionModel(controlPointsSelectionModel);
        controlPointsSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                handleControlPointSelectionChange();
            }
        });
        TextColumn<ControlPointAndOldAndNewMark> nameColumn = new TextColumn<ControlPointAndOldAndNewMark>() {
            @Override
            public String getValue(ControlPointAndOldAndNewMark cpaoanm) {
                return cpaoanm.getControlPoint().getName();
            }
        };
        controlPointsTable.addColumn(nameColumn, stringMessages.controlPoint());
        TextColumn<ControlPointAndOldAndNewMark> passingInstructionsColumn = new TextColumn<ControlPointAndOldAndNewMark>() {
            @Override
            public String getValue(ControlPointAndOldAndNewMark cpaoanm) {
                String result = "";
                if(cpaoanm.getPassingInstructions() != null) {
                    result = cpaoanm.getPassingInstructions().name();
                }
                return result;
            }
        };        
        controlPointsTable.addColumn(passingInstructionsColumn, "Passing instructions");
        TextColumn<ControlPointAndOldAndNewMark> oldMarkColumn = new TextColumn<ControlPointAndOldAndNewMark>() {
            @Override
            public String getValue(ControlPointAndOldAndNewMark cpaoanm) {
                return "" + cpaoanm.getOldMark().getName();
            }
        }; 
        controlPointsTable.addColumn(oldMarkColumn, stringMessages.mark());
        TextColumn<ControlPointAndOldAndNewMark> newMarkColumn = new TextColumn<ControlPointAndOldAndNewMark>(){

            @Override
            public String getValue(ControlPointAndOldAndNewMark cpaoanm) {
                return "" + cpaoanm.getNewMark().getName();
            }
            
        }; 
        controlPointsTable.addColumn(newMarkColumn, stringMessages.newMark());
        controlPointDataProvider = new ListDataProvider<ControlPointAndOldAndNewMark>();
        controlPointDataProvider.addDataDisplay(controlPointsTable);

        // race course marks table
       markDataProvider = new ListDataProvider<MarkDTO>();
        markSelectionChangeHandler = new Handler() {
            @Override
           public void onSelectionChange(SelectionChangeEvent event) {
                if (!ignoreWaypointAndOldAndNewMarkSelectionChange) {
                    updateNewMark(controlPointsSelectionModel.getSelectedSet(), markSelectionModel.getSelectedObject());
                }
            }
        };
        markSelectionModel = new SingleSelectionModel<MarkDTO>();
        marksTable = createAvailableMarksTable(stringMessages, tableRes, markSelectionModel);
        marksTable.getSelectionModel().addSelectionChangeHandler(markSelectionChangeHandler);
        markDataProvider.addDataDisplay(marksTable);
        grid.setWidget(1,  1, marksTable);
        
        courseActionsPanel = new HorizontalPanel();
        courseActionsPanel.setSpacing(10);
        insertWaypointBefore = new Button(stringMessages.insertWaypointBeforeSelected());
        insertWaypointBefore.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
               insertWaypoint(sailingService, stringMessages, tableRes, /* before */ true);
            }

        });
        insertWaypointBefore.setEnabled(false);
        courseActionsPanel.add(insertWaypointBefore);
        insertWaypointAfter = new Button(stringMessages.insertWaypointAfterSelected());
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
               refreshSelectedRaceData ();
            }
        });
        courseActionsPanel.add(refreshBtn);
        Button saveBtn = new Button(stringMessages.save());
        saveBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveCourse(sailingService, stringMessages);
            }
        });        courseActionsPanel.add(saveBtn);
        courseActionsPanel.setVisible(false);
        this.selectedRaceContentPanel.add(courseActionsPanel);
    }

    private void handleControlPointSelectionChange() {
        ignoreWaypointAndOldAndNewMarkSelectionChange = true;
        try {
            markSelectionModel.setSelected(markSelectionModel.getSelectedObject(), false);
          final int selectionSize = controlPointsSelectionModel.getSelectedSet().size();
            insertWaypointAfter.setEnabled(selectionSize==1);
            insertWaypointBefore.setEnabled(selectionSize==1);
            removeWaypointBtn.setEnabled(selectionSize>=1);
            if (selectionSize == 1) {
                MarkDTO newMark = controlPointsSelectionModel.getSelectedSet().iterator().next().getNewMark();
                if (newMark != null) {
                    for (MarkDTO markDTO : markDataProvider.getList()) {
                        if (markDTO.getName().equals(newMark.getName())) {
                            markSelectionModel.setSelected(markDTO, true);
                        }
                    }
                }
            }
        } finally {
            ignoreWaypointAndOldAndNewMarkSelectionChange = false;
        }
    }

    private void saveCourse(SailingServiceAsync sailingService, final StringMessages stringMessages) {
        Set<ControlPointDTO> oldControlPointsFromTableAlreadyHandled = new HashSet<ControlPointDTO>();
        List<Pair<ControlPointDTO, PassingInstruction>> controlPoints = new ArrayList<Pair<ControlPointDTO, PassingInstruction>>();
        for (ControlPointAndOldAndNewMark cpaoanb : controlPointDataProvider.getList()) {
            if (!oldControlPointsFromTableAlreadyHandled.contains(cpaoanb.getControlPoint())) {
                oldControlPointsFromTableAlreadyHandled.add(cpaoanb.getControlPoint());
                ControlPointDTO controlPointToAdd;
                if (controlPointsNeedingReplacement.contains(cpaoanb.getControlPoint())) {
                    if (cpaoanb.getControlPoint() instanceof GateDTO) {
                        controlPointToAdd = createGate((GateDTO) cpaoanb.getControlPoint());
                    } else {
                        controlPointToAdd = cpaoanb.getNewMark();
                    }
                } else {
                    controlPointToAdd = cpaoanb.getControlPoint();
                }
                controlPoints.add(new Pair<ControlPointDTO, PassingInstruction>(controlPointToAdd, cpaoanb.passingInstructions));
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
                refreshSelectedRaceData();
            }
        });
    }

    /**
     * When a gate needs replacement, its entries in {@link #controlPointDataProvider} are looked up, and a new
     * {@link GateDTO} is created having the same name as the old gate, but using the new marks as the gate's marks.
     */
    private ControlPointDTO createGate(GateDTO oldGate) {
        MarkDTO newLeft = null;
        MarkDTO newRight = null;
        for (ControlPointAndOldAndNewMark cpaoanb : controlPointDataProvider.getList()) {
            if (cpaoanb.getControlPoint() == oldGate) {
                MarkDTO newMark = cpaoanb.getNewMark();
                if (newRight != null || newMark.getName().matches("^.*"+REGEX_FOR_LEFT+".*$")) {
                    newLeft = newMark;
                } else {
                    newRight = newMark;
                }
            }
        }
        assert newLeft != null && newRight != null;
        // if old gate had null ID, the new gate will have a null ID too, causing the server to generate one
        return new GateDTO(oldGate.getIdAsString(), oldGate.getName(), newLeft, newRight);
    }

    private CellTable<MarkDTO> createAvailableMarksTable(final StringMessages stringMessages, AdminConsoleTableResources tableRes,
            SelectionModel<MarkDTO> selectionModel) {
        CellTable<MarkDTO> result = new CellTable<MarkDTO>(/* pageSize */10000, tableRes);
        result.setSelectionModel(selectionModel);
        TextColumn<MarkDTO> markNameColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.getName();
            }
        };
        result.addColumn(markNameColumn, stringMessages.mark());

        final SafeHtmlCell markPositionCell = new SafeHtmlCell();
        Column<MarkDTO, SafeHtml> markPositionColumn = new Column<MarkDTO, SafeHtml>(markPositionCell) {
            @Override
            public SafeHtml getValue(MarkDTO mark) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                if(mark.position != null) {
                    NumberFormat fmt = NumberFormat.getFormat("#.###");
                    builder.appendEscaped(fmt.format(mark.position.latDeg)+", "+fmt.format(mark.position.lngDeg));
                }
                return builder.toSafeHtml();
            }
        };
        result.addColumn(markPositionColumn, stringMessages.position());
        
        TextColumn<MarkDTO> markColorColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.color != null ? markDTO.color : "";
            }
        };
        result.addColumn(markColorColumn, stringMessages.color());

        TextColumn<MarkDTO> markShapeColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.shape != null ? markDTO.shape : "";
            }
        };
        result.addColumn(markShapeColumn, stringMessages.shape());

        TextColumn<MarkDTO> markPatternColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.pattern != null ? markDTO.pattern : "";
            }
        };
        result.addColumn(markPatternColumn, stringMessages.pattern());

        TextColumn<MarkDTO> markUUIDColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.getIdAsString();
            }
        };
        result.addColumn(markUUIDColumn, "UUID");
        
        return result;
    }

    private void updateNewMark(Set<ControlPointAndOldAndNewMark> selectedWaypointsAndOldAndNewMarks, MarkDTO selectedNewMark) {
        if (selectedWaypointsAndOldAndNewMarks != null) {
            for (ControlPointAndOldAndNewMark w : selectedWaypointsAndOldAndNewMarks) {
                if (selectedNewMark == null) {
                    w.setNewMark(w.getOldMark());
                } else {
                    w.setNewMark(selectedNewMark);
                }
                if (w.getOldMark().getName().equals(w.getNewMark().getName())) {
                    checkIfAllMarksOfControlPointAreUnchangedAndIfSoRememberThis(w.getControlPoint());
                } else {
                    controlPointsNeedingReplacement.add(w.getControlPoint());
                }
                final int indexOf = controlPointDataProvider.getList().indexOf(w);
                if (indexOf != -1) {
                    controlPointDataProvider.getList().set(indexOf, w);
                }
            }
        }
    }

    private void checkIfAllMarksOfControlPointAreUnchangedAndIfSoRememberThis(ControlPointDTO controlPoint) {
        boolean allMarksUnchanged = true;
        for (ControlPointAndOldAndNewMark cpaoanb : controlPointDataProvider.getList()) {
            if (cpaoanb.getControlPoint() == controlPoint) {
                if (!cpaoanb.getOldMark().getName().equals(cpaoanb.getNewMark().getName())) {
                    allMarksUnchanged = false;
                    break;
                }
            }
        }
        if (allMarksUnchanged) {
            controlPointsNeedingReplacement.remove(controlPoint);
        }
    }

    private void insertWaypoint(final SailingServiceAsync sailingService, StringMessages stringMessages,
            AdminConsoleTableResources tableRes, final boolean beforeSelection) {
        new ControlPointCreationDialog(stringMessages, tableRes, markDataProvider.getList(), new DialogCallback<ControlPointDTO>() {
            @Override
            public void cancel() {
                // dialog cancelled, do nothing
            }

            @Override
            public void ok(ControlPointDTO result) {
                Set<ControlPointAndOldAndNewMark> selectedElements = controlPointsSelectionModel.getSelectedSet();
                if (!selectedElements.isEmpty()) {
                    ControlPointAndOldAndNewMark selectedElement = selectedElements.iterator().next();
                    int insertPos = controlPointDataProvider.getList().indexOf(selectedElement) + (beforeSelection?0:1);
                    for (MarkDTO markDTO : result.getMarks()) {
                        controlPointDataProvider.getList().add(insertPos++, new ControlPointAndOldAndNewMark(result, null, markDTO));
                    }
                }
            }
        }).show();
    }

    @Override
    void refreshSelectedRaceData() {
        if (singleSelectedRace != null && selectedRaceDTO != null) {
            courseActionsPanel.setVisible(true);
            // TODO bug 1351: never use System.currentTimeMillis() on the client when trying to compare anything with "server time"; this one is not so urgent as it is reached only in the AdminConsole and we expect administrators to have proper client-side time settings
            sailingService.getRaceCourse(singleSelectedRace, new Date(),  new AsyncCallback<RaceCourseDTO>() {
                @Override
                public void onSuccess(RaceCourseDTO raceCourseDTO) {
                    updateCourseAndMarksInfo(raceCourseDTO);
                }
    
                @Override
                public void onFailure(Throwable caught) {
                    RaceCourseManagementPanel.this.errorReporter.reportError(
                            RaceCourseManagementPanel.this.stringMessages.errorTryingToObtainTheMarksOfTheRace(
                            caught.getMessage()));
                }
            });
        } else {
            courseActionsPanel.setVisible(false);
        }
    }

    private void updateCourseAndMarksInfo(RaceCourseDTO raceCourseDTO) {
        List<ControlPointAndOldAndNewMark> waypointsAndOldAndNewMarks = new ArrayList<ControlPointAndOldAndNewMark>();
        for (WaypointDTO waypointDTO : raceCourseDTO.waypoints) {
            ControlPointDTO controlPointDTO = waypointDTO.controlPoint;
            for (MarkDTO mark : controlPointDTO.getMarks()) {
                ControlPointAndOldAndNewMark waypointAndOldAndNewMark = new ControlPointAndOldAndNewMark(controlPointDTO, waypointDTO.passingInstructions, mark);
                waypointsAndOldAndNewMarks.add(waypointAndOldAndNewMark);
            }
        }
        controlPointDataProvider.getList().clear();
        controlPointDataProvider.getList().addAll(waypointsAndOldAndNewMarks);
        controlPointsNeedingReplacement.clear();
        markDataProvider.getList().clear();
        markDataProvider.getList().addAll(raceCourseDTO.getMarks());
        Collections.sort(markDataProvider.getList(), new Comparator<MarkDTO>() {
            @Override
            public int compare(MarkDTO o1, MarkDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (ControlPointAndOldAndNewMark w : controlPointsSelectionModel.getSelectedSet()) {
            controlPointsSelectionModel.setSelected(w, false);
        }
    }

    private void removeSelectedWaypoints(final SailingServiceAsync sailingService) {
        final Set<ControlPointDTO> selectedControlPoints = new HashSet<ControlPointDTO>();
        for (ControlPointAndOldAndNewMark cpaoanb : controlPointsSelectionModel.getSelectedSet()) {
            selectedControlPoints.add(cpaoanb.getControlPoint());
        }
        for (Iterator<ControlPointAndOldAndNewMark> i=controlPointDataProvider.getList().iterator(); i.hasNext(); ) {
            ControlPointAndOldAndNewMark next = i.next();
            if (selectedControlPoints.contains(next.getControlPoint())) {
                i.remove();
            }
        }
        handleControlPointSelectionChange();
    }
}
