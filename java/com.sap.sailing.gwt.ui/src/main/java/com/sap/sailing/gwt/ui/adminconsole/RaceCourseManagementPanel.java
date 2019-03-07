package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;

/**
 * A panel that has a race selection (inherited from {@link AbstractRaceManagementPanel}) and which adds a table
 * for a selected race showing the race's waypoints together with the number of mark passings already received for that
 * waypoint. Also, the control can be used to send course updates into the tracked race, mostly to simulate these types
 * of events. Conceivably, this may in the future also become a way to set up and edit courses for a tracked race.
 * 
 * @author Frank Mittag (C5163874), Axel Uhl (D043530)
 */
public class RaceCourseManagementPanel extends AbstractRaceManagementPanel {
    private final CourseManagementWidget courseManagementWidget;

    public RaceCourseManagementPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, final StringMessages stringMessages, final UserService userService) {
        super(sailingService, userService, errorReporter, regattaRefresher, /* actionButtonsEnabled */ false,
                stringMessages);
        
        courseManagementWidget = new CourseManagementWidget(sailingService, errorReporter, stringMessages,
                userService) {
            @Override
            protected void save() {
                sailingService.updateRaceCourse(singleSelectedRace, createWaypointPairs(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorUpdatingRaceCourse(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify(stringMessages.successfullyUpdatedCourse(), NotificationType.INFO);
                        refreshSelectedRaceData();
                    }
                });
            }
            
            @Override
            public void refresh() {
                if (singleSelectedRace != null && selectedRaceDTO != null) {
                    mainPanel.setVisible(true);
                    // TODO bug 1351: never use System.currentTimeMillis() on the client when trying to compare anything with "server time"; this one is not so urgent as it is reached only in the AdminConsole and we expect administrators to have proper client-side time settings
                    sailingService.getRaceCourse(singleSelectedRace, new Date(),  new AsyncCallback<RaceCourseDTO>() {
                        @Override
                        public void onSuccess(RaceCourseDTO raceCourseDTO) {
                            updateWaypointsAndControlPoints(raceCourseDTO, selectedRaceDTO);
                            marks.refresh(raceCourseDTO.getMarks());
                        }
            
                        @Override
                        public void onFailure(Throwable caught) {
                            RaceCourseManagementPanel.this.errorReporter.reportError(
                                    RaceCourseManagementPanel.this.stringMessages.errorTryingToObtainTheMarksOfTheRace(
                                    caught.getMessage()));
                        }
                    });
                } else {
                    mainPanel.setVisible(false);
                }
            }
        };
        
        FlowPanel courseManagementPanel = new FlowPanel();
        courseManagementPanel.add(courseManagementWidget);

        HorizontalPanel buttonsPanel = new HorizontalPanel();

        Button refreshBtn = new Button(stringMessages.refresh());
        refreshBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    courseManagementWidget.refresh();
                }
        });
        
        buttonsPanel.add(refreshBtn);
        
        Button saveBtn = new Button(stringMessages.save());
        saveBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    courseManagementWidget.save();
                }
            });
        
        trackedRacesListComposite.getSelectionModel().addSelectionChangeHandler(h -> {
            saveBtn.setVisible(userService.hasPermission(selectedRaceDTO, DefaultActions.UPDATE));
        });

        buttonsPanel.add(saveBtn);
        this.selectedRaceContentPanel.add(courseManagementWidget);
        this.selectedRaceContentPanel.add(buttonsPanel);
    }

    @Override
    void refreshSelectedRaceData() {
        courseManagementWidget.refresh();
    }
}
