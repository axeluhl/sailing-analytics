package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

public class RaceLogTrackingCourseDefinitionDialog extends RaceLogTrackingDialog {
    private class RaceLogCourseManagementWidget extends CourseManagementWidget {
        public RaceLogCourseManagementWidget(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
                final StringMessages stringMessages) {
            super(sailingService, errorReporter, stringMessages);
            
            Button addMark = new Button(stringMessages.add(stringMessages.mark()));
            addMark.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new MarkEditDialog(stringMessages, new MarkDTO(null, null), true, new DialogCallback<MarkDTO>() {
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
            courseActionsPanel.insert(addMark, courseActionsPanel.getWidgetIndex(RaceLogCourseManagementWidget.this.saveButton));
            
            Button cancel = new Button(stringMessages.cancel());
            cancel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                }
            });
            courseActionsPanel.insert(cancel, courseActionsPanel.getWidgetIndex(RaceLogCourseManagementWidget.this.saveButton));
        }

        @Override
        protected void saveCourse(List<Pair<ControlPointDTO, PassingInstruction>> controlPoints) {
            sailingService.addCourseDefinitionToRaceLog(leaderboardName, raceColumnName, fleetName, controlPoints, new AsyncCallback<Void>() {
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
                    updateWaypointTable(result);
                    if (result.waypoints.isEmpty()) {
                        insertWaypointBefore.setEnabled(true);
                    }
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Could not load course: " + caught.getMessage());
                }
            });
            
            sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Collection<MarkDTO>>() {
                @Override
                public void onSuccess(Collection<MarkDTO> result) {
                    updateMarksTable(result);
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
