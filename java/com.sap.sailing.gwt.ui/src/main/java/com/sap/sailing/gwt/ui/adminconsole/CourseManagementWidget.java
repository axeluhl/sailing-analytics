package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SetSelectionModel;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.adminconsole.WaypointCreationDialog.DefaultPassingInstructionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;

public abstract class CourseManagementWidget implements IsWidget {
    protected final MarkTableWrapper<RefreshableMultiSelectionModel<MarkDTO>> marks;
    protected final ControlPointTableWrapper<RefreshableSingleSelectionModel<ControlPointDTO>> multiMarkControlPoints;
    protected final WaypointTableWrapper<RefreshableSingleSelectionModel<WaypointDTO>> waypoints;
    
    protected final Grid mainPanel;
    
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final StringMessages stringMessages;
    
    protected final HorizontalPanel waypointsBtnsPanel;
    protected final HorizontalPanel controlPointsBtnsPanel;
    protected final HorizontalPanel marksBtnsPanel;
    
    protected final Button insertWaypointBefore;
    protected final Button insertWaypointAfter;
    private PassingInstruction lastSingleMarkPassingInstruction = PassingInstruction.Port; // the usual default
    
    protected final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    
    private SecuredDTO securedDtoForWaypointsPermissionCheck;

    @Override
    public Widget asWidget() {
        return mainPanel;
    }
    
    public CourseManagementWidget(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserService userService) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        mainPanel = new Grid(3, 3);
        mainPanel.setCellPadding(5);
        mainPanel.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        
        waypoints = new WaypointTableWrapper<RefreshableSingleSelectionModel<WaypointDTO>>(
                /* multiSelection */ false, sailingService, stringMessages, errorReporter);
        multiMarkControlPoints = new ControlPointTableWrapper<RefreshableSingleSelectionModel<ControlPointDTO>>(
                /* multiSelection */ false, sailingService, stringMessages, errorReporter);
        marks = new MarkTableWrapper<RefreshableMultiSelectionModel<MarkDTO>>(
                /* multiSelection */ true, sailingService, stringMessages, errorReporter);
        
        marks.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                markSelectionChanged();
            }
        });
        
        CaptionPanel waypointsPanel = new CaptionPanel(stringMessages.waypoints());
        CaptionPanel controlPointsPanel = new CaptionPanel(stringMessages.twoMarkControlPoint());
        CaptionPanel marksPanel = new CaptionPanel(stringMessages.mark());
        waypointsPanel.add(waypoints);
        controlPointsPanel.add(multiMarkControlPoints);
        marksPanel.add(marks);
        mainPanel.setWidget(0, 0, waypointsPanel);
        mainPanel.setWidget(0, 1, controlPointsPanel);
        mainPanel.setWidget(0, 2, marksPanel);
        
        waypointsBtnsPanel = new HorizontalPanel();
        controlPointsBtnsPanel = new HorizontalPanel();
        marksBtnsPanel = new HorizontalPanel();
        mainPanel.setWidget(1, 0, waypointsBtnsPanel);
        mainPanel.setWidget(1, 1, controlPointsBtnsPanel);
        mainPanel.setWidget(1, 2, marksBtnsPanel);
        
        final AccessControlledActionsColumn<WaypointDTO, DefaultActionsImagesBarCell> waypointsActionColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService,
                s -> securedDtoForWaypointsPermissionCheck);

        // update permission for tracked race is required for deleting waypoints
        waypointsActionColumn.addAction(DefaultActions.DELETE.name(), DefaultActions.UPDATE,
                waypoint -> removeWaypoint(waypoint));

        waypoints.getTable().addColumn(waypointsActionColumn);
        
        waypoints.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                WaypointDTO waypoint = waypoints.getSelectionModel().getSelectedObject();
                if (waypoint != null) {
                    selectControlPoints(waypoint);
                    selectMarks(waypoint.controlPoint.getMarks());
                }
                updateWaypointButtons();
            }
        });
        
        multiMarkControlPoints.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (multiMarkControlPoints.getSelectionModel().getSelectedSet().size() > 0) {
                    ControlPointDTO first = multiMarkControlPoints.getSelectionModel().getSelectedSet().iterator().next();
                    selectMarks(first.getMarks());
                }
            }
        });
        
        insertWaypointBefore = new Button(stringMessages.insertWaypointBeforeSelected());
        insertWaypointBefore.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                insertWaypoint(/* before */ true);
            }
        });
        insertWaypointBefore.setEnabled(false);
        waypointsBtnsPanel.add(insertWaypointBefore);
        insertWaypointAfter = new Button(stringMessages.insertWaypointAfterSelected());
        insertWaypointAfter.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                insertWaypoint(/* before */ false);
            }
        });
        insertWaypointAfter.setEnabled(false);
        waypointsBtnsPanel.add(insertWaypointAfter);
        
        final Button addControlPoint = new Button(stringMessages.add(stringMessages.twoMarkControlPoint()));
        addControlPoint.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addMultiMarkControlPoint();
            }
        });
        controlPointsBtnsPanel.add(addControlPoint);
    }
    
    protected void markSelectionChanged() {
    }

    private void removeWaypoint(WaypointDTO waypoint) {
        waypoints.getDataProvider().getList().remove(waypoint);
    }
    
    private void selectControlPoints(WaypointDTO waypoint) {
        for (ControlPointDTO controlPoint : multiMarkControlPoints.getDataProvider().getList()) {
            multiMarkControlPoints.getSelectionModel().setSelected(
                    controlPoint, waypoint.controlPoint == controlPoint);
        }
    }
    
    private void selectMarks(Iterable<MarkDTO> newMarks) {
        marks.getSelectionModel().clear();
        for (MarkDTO toSelect : newMarks) {
            marks.getSelectionModel().setSelected(toSelect, true);
        }
    }

    protected void save(){};
    
    private <T> T getFirstSelected(SetSelectionModel<T> selectionModel) {
        if (selectionModel.getSelectedSet().isEmpty()) {
            return null;
        }
        return selectionModel.getSelectedSet().iterator().next();
    }
    
    private <T> void insert(TableWrapper<T, ? extends SetSelectionModel<T>> tableWrapper, T toInsert, boolean beforeSelection) {
        int index = getInsertIndex(tableWrapper, beforeSelection);
        if (index != -1) {
            tableWrapper.getDataProvider().getList().add(index, toInsert);
        }
    }

    private <T> int getInsertIndex(TableWrapper<T, ? extends SetSelectionModel<T>> tableWrapper,
            boolean beforeSelection) {
        T selected = getFirstSelected(tableWrapper.getSelectionModel());
        int index = -1;
        if (tableWrapper.getDataProvider().getList().isEmpty()) {
            index = 0;
        } else if (selected != null) {
            index = tableWrapper.getDataProvider().getList().indexOf(selected) + (beforeSelection?0:1);
        }
        return index;
    }

    private void addMultiMarkControlPoint() {
        new GateCreationDialog(sailingService, errorReporter, stringMessages, tableRes,
                marks.getDataProvider().getList(), new DataEntryDialog.DialogCallback<GateDTO>() {
            @Override
            public void cancel() {}

            @Override
            public void ok(GateDTO result) {
                multiMarkControlPoints.getDataProvider().getList().add(result);
            }
        }).show();
    }
    
    private void insertWaypoint(final boolean beforeSelection) {
        List<ControlPointDTO> allControlPoints = new ArrayList<>();
        allControlPoints.addAll(multiMarkControlPoints.getDataProvider().getList());
        allControlPoints.addAll(marks.getDataProvider().getList());
        final int insertIndex = getInsertIndex(waypoints, beforeSelection);
        new WaypointCreationDialog(sailingService, errorReporter, stringMessages, tableRes, allControlPoints,
                new DefaultPassingInstructionProvider() {
                    @Override
                    public PassingInstruction getDefaultPassingInstruction(int numberOfMarksInControlPoint, String controlPointIdAsString) {
                        final PassingInstruction result;
                        if (numberOfMarksInControlPoint == 2) {
                            if (insertIndex == 0 ||
                                    (!waypoints.getDataProvider().getList().isEmpty() && Util.equalsWithNull(controlPointIdAsString, waypoints.getDataProvider().getList().get(0).controlPoint.getIdAsString())
                                    && insertIndex == waypoints.getDataProvider().getList().size())) {
                                // two-mark control point used as first waypoint or
                                // same control point as for the first waypoint now used as the last one;
                                // suggest Line:
                                result = PassingInstruction.Line;
                            } else {
                                result = PassingInstruction.Gate;
                            }
                        } else if (numberOfMarksInControlPoint == 1) {
                            result = lastSingleMarkPassingInstruction;
                        } else {
                            result = null;
                        }
                        return result;
                    }
                }, new DataEntryDialog.DialogCallback<WaypointDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(WaypointDTO result) {
                        insert(waypoints, result, beforeSelection);
                        waypoints.getSelectionModel().setSelected(result, true);
                        if (Util.size(result.controlPoint.getMarks()) == 1) {
                            lastSingleMarkPassingInstruction = result.passingInstructions;
                        }
                    }
                }).show();
    }

    public void refresh(){};

    protected void updateWaypointsAndControlPoints(RaceCourseDTO raceCourseDTO, String leaderboardName) {
        this.sailingService.getLeaderboardWithSecurity(leaderboardName,
                new AsyncCallback<StrippedLeaderboardDTOWithSecurity>() {
            @Override
            public void onFailure(Throwable caught) {
                        Window.alert(caught.getMessage());
            }

            @Override
                    public void onSuccess(StrippedLeaderboardDTOWithSecurity result) {
                        securedDtoForWaypointsPermissionCheck = result;
                        updateWaypointsAndContorlPoints(raceCourseDTO);
            }
        });
    }

    protected void updateWaypointsAndControlPoints(RaceCourseDTO raceCourseDTO, RaceDTO raceDTO) {
        updateWaypointsAndContorlPoints(raceCourseDTO);
        securedDtoForWaypointsPermissionCheck = raceDTO;
    }

    private void updateWaypointsAndContorlPoints(RaceCourseDTO raceCourseDTO) {
        waypoints.getDataProvider().getList().clear();
        multiMarkControlPoints.getDataProvider().getList().clear();
        waypoints.getDataProvider().getList().addAll(raceCourseDTO.waypoints);
        
        Map<String, ControlPointDTO> noDuplicateCPs = new HashMap<>();
        for (ControlPointDTO controlPoint : raceCourseDTO.getControlPoints()) {
            if (controlPoint instanceof GateDTO) {
                noDuplicateCPs.put(controlPoint.getIdAsString(), controlPoint);
            }
        }
        multiMarkControlPoints.getDataProvider().getList().addAll(noDuplicateCPs.values());
        
        updateWaypointButtons();
    }
    
    protected List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>> createWaypointPairs() {
        List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>> result = new ArrayList<>();
        for (WaypointDTO waypoint : waypoints.getDataProvider().getList()) {
            result.add(new com.sap.sse.common.Util.Pair<>(waypoint.controlPoint, waypoint.passingInstructions));
        }
        return result;
    }
    
    protected void updateWaypointButtons() {
        if (waypoints.getDataProvider().getList().isEmpty() ||
                waypoints.getSelectionModel().getSelectedObject() != null) {
            insertWaypointAfter.setEnabled(true);
            insertWaypointBefore.setEnabled(true);
        } else {
            insertWaypointBefore.setEnabled(false);
            insertWaypointAfter.setEnabled(false);
        }
    }
}
