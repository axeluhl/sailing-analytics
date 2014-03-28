package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class CourseManagementWidget implements IsWidget {
    /**
     * Represents one mark assignment for a control point. Gates have multiple such records, one for each of their marks.
     * 
     * @author Axel Uhl (D043530)
     */
    public static class ControlPointAndOldAndNewMark {
        protected final ControlPointDTO controlPoint;
        protected final MarkDTO oldMark;
        protected MarkDTO newMark;
        protected PassingInstruction passingInstructions;
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
    
    private class ControlPointCreationDialog extends DataEntryDialog<Pair<ControlPointDTO, PassingInstruction>> {
        private final MarkTableWrapper<MultiSelectionModel<MarkDTO>> marksTable;
        private final MultiSelectionModel<MarkDTO> selectionModel;
        private final ListBox passingInstructions;
        private final StringMessages stringMessages;
        
        public ControlPointCreationDialog(final StringMessages stringMessages, AdminConsoleTableResources tableRes,
                List<MarkDTO> marks, DialogCallback<Pair<ControlPointDTO, PassingInstruction>> callback) {
            super(stringMessages.controlPoint(), stringMessages.selectOneMarkOrTwoMarksForGate(),
                    stringMessages.ok(), stringMessages.cancel(), new DataEntryDialog.Validator<Pair<ControlPointDTO, PassingInstruction>>() {
                        @Override
                        public String getErrorMessage(Pair<ControlPointDTO, PassingInstruction> valueToValidate) {
                            if (valueToValidate.getA() == null) {
                                return stringMessages.selectOneMarkOrTwoMarksForGate();
                            } else {
                                return null;
                            }
                        }

                    }, /* animationEnabled */ false, callback);
            
            this.stringMessages = stringMessages;
            
            selectionModel = new MultiSelectionModel<MarkDTO>();
            selectionModel.addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    validate();
                }
            });
            marksTable = new MarkTableWrapper<MultiSelectionModel<MarkDTO>>(
                    selectionModel, CourseManagementWidget.this.sailingService, stringMessages, errorReporter);
            marksTable.getDataProvider().getList().addAll(marks);
            
            passingInstructions = createListBox(false);
            passingInstructions.insertItem(PassingInstruction.None.name(), 0);
            int i = 1;
            for (PassingInstruction pi : PassingInstruction.relevantValues()) {
                passingInstructions.insertItem(pi.name(), i++);
            }
        }

        @Override
        protected Pair<ControlPointDTO, PassingInstruction> getResult() {
            ControlPointDTO controlPoint = null;
            Set<MarkDTO> selection = selectionModel.getSelectedSet();
            if (selection.size() == 1) {
                controlPoint = selectionModel.getSelectedSet().iterator().next();
                passingInstructions.setEnabled(true);
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
                controlPoint = new GateDTO(/* generate UUID on the server */ null, gateName, left, right);
                
                passingInstructions.setSelectedIndex(0);
                passingInstructions.setEnabled(false);
            }
            
            PassingInstruction passingInstruction = PassingInstruction.valueOf(
                    passingInstructions.getValue(passingInstructions.getSelectedIndex()));
            
            return new Pair<ControlPointDTO, PassingInstruction>(controlPoint, passingInstruction);
        }

        @Override
        protected Widget getAdditionalWidget() {
            Grid grid = new Grid(2,1);
            grid.setWidget(0, 0, marksTable);
            
            HorizontalPanel passingInstructionsRow = new HorizontalPanel();
            grid.setWidget(1, 0, passingInstructionsRow);
            passingInstructionsRow.add(new Label(stringMessages.passingInstructions() + ":"));
            passingInstructionsRow.add(passingInstructions);
            
            return grid;
        }
    }
    
    protected static final String REGEX_FOR_LEFT = "( \\()?(([lL][eE][fF][tT])|(1))\\)?";

    /**
     * A table that lists the marks for which events have been received for the race selected. Note that this list may
     * be longer than the list of marks actually used by the control points backing the course's waypoints because of
     * the possibility of spare marks.
     */
    protected final SingleSelectionModel<MarkDTO> markSelectionModel;

    /**
     * A table that lists the product of Waypoint x ControlPoint x Mark plus a hint as to the number of mark passings.
     * The (multi-)selection on this table can be used as either a selection of waypoints or a selection of control points
     * or a selection of marks.
     */
    protected final CellTable<ControlPointAndOldAndNewMark> controlPointsTable;
    protected final MultiSelectionModel<ControlPointAndOldAndNewMark> controlPointsSelectionModel; 
    protected final ListDataProvider<ControlPointAndOldAndNewMark> controlPointDataProvider;
    
    /**
     * When for a control point's mark a replacement mark is defined (see {@link #updateNewMark(Set, MarkDTO)}),
     * the control point needs to be replaced before {@link #saveCourse(SailingServiceAsync, StringMessages) saving}.
     * Those control points are added to this set. When the mark is reset to the original mark for all the control
     * point's marks, the control point is removed from this set again. {@link #saveCourse(SailingServiceAsync, StringMessages)}
     * then is responsible for creating replacement {@link ControlPointDTO}s before sending the new control point list to the
     * server.
     */
    protected final Set<ControlPointDTO> controlPointsNeedingReplacement;

    protected final HorizontalPanel courseActionsPanel;
    
    protected final VerticalPanel mainPanel;
    
    protected MarkTableWrapper<SingleSelectionModel<MarkDTO>> marksTable;
    
    protected final Handler markSelectionChangeHandler;
    protected final Button insertWaypointBefore;
    protected final Button insertWaypointAfter;
    protected final Button saveButton;
    protected boolean ignoreWaypointAndOldAndNewMarkSelectionChange;
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    
    @Override
    public Widget asWidget() {
        return mainPanel;
    }
    
    public CourseManagementWidget(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        mainPanel = new VerticalPanel();
        
        controlPointsNeedingReplacement = new HashSet<ControlPointDTO>();
        Grid grid = new Grid(2, 2);
        grid.setCellPadding(5);

        mainPanel.add(grid);
        
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

        ImagesBarColumn<ControlPointAndOldAndNewMark, CourseManagementWidgetWaypointsImagesBarCell> actionColumn =
                new ImagesBarColumn<ControlPointAndOldAndNewMark, CourseManagementWidgetWaypointsImagesBarCell>(
                new CourseManagementWidgetWaypointsImagesBarCell(stringMessages));
        actionColumn.setFieldUpdater(new FieldUpdater<ControlPointAndOldAndNewMark, String>() {
            @Override
            public void update(int index, ControlPointAndOldAndNewMark controlPoint, String value) {
                if (CourseManagementWidgetWaypointsImagesBarCell.ACTION_DELETE.equals(value)) {
                    removeSelectedWaypoints(sailingService);
                }
            }
        });
        controlPointsTable.addColumn(actionColumn, stringMessages.actions());
        controlPointDataProvider = new ListDataProvider<ControlPointAndOldAndNewMark>();
        controlPointDataProvider.addDataDisplay(controlPointsTable);

        // race course marks table
        markSelectionModel = new SingleSelectionModel<MarkDTO>();
        marksTable = new MarkTableWrapper<SingleSelectionModel<MarkDTO>>(markSelectionModel, sailingService, stringMessages, errorReporter);
        markSelectionChangeHandler = new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!ignoreWaypointAndOldAndNewMarkSelectionChange) {
                    updateNewMark(controlPointsSelectionModel.getSelectedSet(), markSelectionModel.getSelectedObject());
                }
            }
        };
        markSelectionModel.addSelectionChangeHandler(markSelectionChangeHandler);
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
        Button refreshBtn = new Button(stringMessages.refresh());
        refreshBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refresh();
            }
        });
        courseActionsPanel.add(refreshBtn);
        saveButton = new Button(stringMessages.save());
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
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
                saveCourse(controlPoints);
            }
        });        courseActionsPanel.add(saveButton);
//        courseActionsPanel.setVisible(false);

        mainPanel.add(courseActionsPanel);
    }

    private void handleControlPointSelectionChange() {
        ignoreWaypointAndOldAndNewMarkSelectionChange = true;
        try {
            markSelectionModel.setSelected(markSelectionModel.getSelectedObject(), false);
          final int selectionSize = controlPointsSelectionModel.getSelectedSet().size();
            insertWaypointAfter.setEnabled(selectionSize==1);
            insertWaypointBefore.setEnabled(selectionSize==1 || controlPointDataProvider.getList().isEmpty());
            if (selectionSize == 1) {
                MarkDTO newMark = controlPointsSelectionModel.getSelectedSet().iterator().next().getNewMark();
                if (newMark != null) {
                    for (MarkDTO markDTO : marksTable.getDataProvider().getList()) {
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

    protected abstract void saveCourse(List<Pair<ControlPointDTO, PassingInstruction>> controlPoints);

    /**
     * When a gate needs replacement, its entries in {@link #controlPointDataProvider} are looked up, and a new
     * {@link GateDTO} is created having the same name as the old gate, but using the new marks as the gate's marks.
     */
    protected ControlPointDTO createGate(GateDTO oldGate) {
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
        new ControlPointCreationDialog(stringMessages, tableRes, marksTable.getDataProvider().getList(), new DataEntryDialog.DialogCallback<Pair<ControlPointDTO, PassingInstruction>>() {
            @Override
            public void cancel() {
                // dialog cancelled, do nothing
            }

            @Override
            public void ok(Pair<ControlPointDTO, PassingInstruction> result) {
                Set<ControlPointAndOldAndNewMark> selectedElements = controlPointsSelectionModel.getSelectedSet();
                int index = -1;
                if (controlPointDataProvider.getList().isEmpty()) {
                    index = 0;
                } else if (!selectedElements.isEmpty()) {
                    ControlPointAndOldAndNewMark selectedElement = selectedElements.iterator().next();
                    index = controlPointDataProvider.getList().indexOf(selectedElement) + (beforeSelection?0:1);
                }
                if (index != -1) {
                    for (MarkDTO markDTO : result.getA().getMarks()) {
                        controlPointDataProvider.getList().add(index++, new ControlPointAndOldAndNewMark(result.getA(), result.getB(), markDTO));
                    }
                }
                handleControlPointSelectionChange();
            }
        }).show();
    }

    public abstract void refresh();

    protected void updateWaypointTable(RaceCourseDTO raceCourseDTO) {
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
                controlPointsSelectionModel.setSelected(next, false);
            }
        }
        handleControlPointSelectionChange();
    }
}
