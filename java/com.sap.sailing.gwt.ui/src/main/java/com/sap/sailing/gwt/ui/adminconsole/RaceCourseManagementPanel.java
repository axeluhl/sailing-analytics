package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
            protected void saveCourse(SailingServiceAsync sailingService, final StringMessages stringMessages) {
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
            
            @Override
            public void refresh() {
                if (singleSelectedRace != null && selectedRaceDTO != null) {
                    courseActionsPanel.setVisible(true);
                    // TODO bug 1351: never use System.currentTimeMillis() on the client when trying to compare anything with "server time"; this one is not so urgent as it is reached only in the AdminConsole and we expect administrators to have proper client-side time settings
                    sailingService.getRaceCourse(singleSelectedRace, new Date(),  new AsyncCallback<RaceCourseDTO>() {
                        @Override
                        public void onSuccess(RaceCourseDTO raceCourseDTO) {
                            updateWaypointTable(raceCourseDTO);
                            updateMarksTable(raceCourseDTO);
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
            
            private void updateMarksTable(RaceCourseDTO raceCourseDTO) {
                markDataProvider.getList().clear();
                markDataProvider.getList().addAll(raceCourseDTO.getMarks());
                Collections.sort(markDataProvider.getList(), new Comparator<MarkDTO>() {
                    @Override
                    public int compare(MarkDTO o1, MarkDTO o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
        };
        this.selectedRaceContentPanel.add(courseManagementWidget);
    }

    @Override
    void refreshSelectedRaceData() {
        courseManagementWidget.refresh();
    }
}
