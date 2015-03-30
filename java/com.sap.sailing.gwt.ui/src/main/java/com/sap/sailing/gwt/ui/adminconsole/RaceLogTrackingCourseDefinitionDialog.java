package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RaceLogTrackingCourseDefinitionDialog extends RaceLogTrackingDialog {
    private class RaceLogCourseManagementWidget extends CourseManagementWidget {
        public RaceLogCourseManagementWidget(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
                final StringMessages stringMessages) {
            super(sailingService, errorReporter, stringMessages);
            
            Button addMark = new Button(stringMessages.add(stringMessages.mark()));
            addMark.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new MarkEditDialog(stringMessages, new MarkDTO(null, null), true, new DataEntryDialog.DialogCallback<MarkDTO>() {
                        @Override
                        public void ok(MarkDTO mark) {
                            sailingService.addMarkToRaceLog(leaderboardName, raceColumnName, fleetName, mark, new AsyncCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    courseManagementWidget.refresh();
                                }
                                
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Could not add mark: " + caught.getMessage());
                                }
                            });
                        }

                        @Override
                        public void cancel() {}
                    }).show();
                }
            });
            marksBtnsPanel.add(addMark);
            
            
            Button cancel = new Button(stringMessages.cancel());
            cancel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                }
            });
            buttonsPanel.add(cancel);
            
            ImagesBarColumn<MarkDTO, RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell> actionColumn =
                    new ImagesBarColumn<MarkDTO, RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell>(
                    new RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell(stringMessages));
            actionColumn.setFieldUpdater(new FieldUpdater<MarkDTO, String>() {
                @Override
                public void update(int index, final MarkDTO markDTO, String value) {
                    if (RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell.ACTION_PING.equals(value)) {
                        new PositionEntryDialog(stringMessages.pingPosition(stringMessages.mark()),
                                stringMessages, new DataEntryDialog.DialogCallback<PositionDTO>() {

                            @Override
                            public void ok(PositionDTO position) {
                                sailingService.pingMarkViaRaceLogTracking(leaderboardName, raceColumnName, fleetName,
                                        markDTO, position, new AsyncCallback<Void>() {
                                            
                                            @Override
                                            public void onSuccess(Void result) {
                                                refresh();
                                            }
                                            
                                            @Override
                                            public void onFailure(Throwable caught) {
                                                errorReporter.reportError("Could not ping mark: " + caught.getMessage());
                                            }
                                        });
                            }

                            @Override
                            public void cancel() {}
                        }).show();
                    }
                }
            });
            marks.getTable().addColumn(actionColumn);
        }

        @Override
        protected void save() {
            sailingService.addCourseDefinitionToRaceLog(leaderboardName, raceColumnName, fleetName,
                    createWaypointPairs(), new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    hide();
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Could note save course: " + caught.getMessage());
                }
            });
        }
        
        @Override
        public void refresh() {
            sailingService.getLastCourseDefinitionInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<RaceCourseDTO>() {
                @Override
                public void onSuccess(RaceCourseDTO result) {
                    updateWaypointsAndControlPoints(result);
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Could not load course: " + caught.getMessage());
                }
            });
            
            sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Iterable<MarkDTO>>() {
                @Override
                public void onSuccess(Iterable<MarkDTO> result) {
                    marks.refresh(result);
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Could not load marks: " + caught.getMessage());
                }
            });
        }
    };
    
    private CourseManagementWidget courseManagementWidget;
    
    public RaceLogTrackingCourseDefinitionDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName, final String raceColumnName, final String fleetName) {
        super(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName);
        setupUi();
        courseManagementWidget.refresh();
    }
    
    @Override
    protected void addMainContent(Panel mainPanel) {
        super.addMainContent(mainPanel);
        
        courseManagementWidget = new RaceLogCourseManagementWidget(sailingService, errorReporter, stringMessages);        
        mainPanel.add(courseManagementWidget);
    }
    
    @Override
    protected void addButtons(Panel buttonPanel) {
        //reuse buttons of widget
    }

    @Override
    protected void save() {
        //won't be called, as we are reusing widget buttons
    }
}
