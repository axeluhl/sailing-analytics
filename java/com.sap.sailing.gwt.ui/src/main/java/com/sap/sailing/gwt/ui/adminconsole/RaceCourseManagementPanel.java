package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;

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
            RegattaRefresher regattaRefresher, final StringMessages stringMessages) {
        super(sailingService, errorReporter, regattaRefresher, stringMessages);
        
        courseManagementWidget = new CourseManagementWidget(sailingService, errorReporter, stringMessages) {
            
            @Override
            protected void saveCourse(List<Pair<ControlPointDTO, PassingInstruction>> controlPoints) {
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
            
            @Override
            public void refresh() {
                if (singleSelectedRace != null && selectedRaceDTO != null) {
//                    courseActionsPanel.setVisible(true);
                    // TODO bug 1351: never use System.currentTimeMillis() on the client when trying to compare anything with "server time"; this one is not so urgent as it is reached only in the AdminConsole and we expect administrators to have proper client-side time settings
                    sailingService.getRaceCourse(singleSelectedRace, new Date(),  new AsyncCallback<RaceCourseDTO>() {
                        @Override
                        public void onSuccess(RaceCourseDTO raceCourseDTO) {
                            updateWaypointTable(raceCourseDTO);
                            marksTable.refresh(raceCourseDTO.getMarks());
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
        };
        this.selectedRaceContentPanel.add(courseManagementWidget);
    }

    @Override
    void refreshSelectedRaceData() {
        courseManagementWidget.refresh();
    }
}
