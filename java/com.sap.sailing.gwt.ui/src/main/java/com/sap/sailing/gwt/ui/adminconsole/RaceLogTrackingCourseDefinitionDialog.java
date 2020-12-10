package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.orc.impl.ORCPerformanceCurveLegImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public class RaceLogTrackingCourseDefinitionDialog extends
        DataEntryDialog<RaceLogTrackingCourseDefinitionDialog.Result> {

    private final CourseManagementWidget courseManagementWidget;
    private final Button refreshButton;
    private final IntegerBox priorityBox;
    private final StringMessages stringMessages;
    
    public static class Result {
        private final List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>> waypoints;
        private final Map<Integer, ORCPerformanceCurveLegImpl> orcLegData;
        private final Integer priority;
        public Result(List<Pair<ControlPointDTO, PassingInstruction>> waypoints, Map<Integer, ORCPerformanceCurveLegImpl> orcLegData, Integer priority) {
            super();
            this.waypoints = waypoints;
            this.orcLegData = orcLegData;
            this.priority = priority;
        }
        public List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>> getWaypoints() {
            return waypoints;
        }
        public Integer getPriority() {
            return priority;
        }
        public Map<Integer, ORCPerformanceCurveLegImpl> getORCLegData() {
            return orcLegData;
        }
    }

    public RaceLogTrackingCourseDefinitionDialog(final SailingServiceWriteAsync sailingServiceWrite,
            final StringMessages stringMessages, final ErrorReporter errorReporter, final String leaderboardName,
            final String raceColumnName, final String fleetName,
            DialogCallback<Result> callback, final UserService userService) {
        super(stringMessages.defineCourse(), null, stringMessages.save(), stringMessages.cancel(),
                new Validator<Result>() {
                    @Override
                    public String getErrorMessage(Result valueToValidate) {
                        final String errorMessage;
                        if (valueToValidate.getPriority() == null || valueToValidate.getPriority() < 0) {
                            errorMessage = stringMessages.priorityMustBeANonNegativeNumber();
                        } else {
                            errorMessage = null;
                        }
                        return errorMessage;
                    }
                },
                callback);
        this.stringMessages = stringMessages;
        courseManagementWidget = new RaceLogCourseManagementWidget(sailingServiceWrite, errorReporter, stringMessages,
                leaderboardName, raceColumnName, fleetName, userService);
        priorityBox = createIntegerBox(/* default priority: race officer */ 1, /* visibleLength */ 1);
        refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() { 
            @Override
            public void onClick(ClickEvent event) {
                courseManagementWidget.refresh();
            }
        });
        courseManagementWidget.refresh();
    }

    @Override
    protected Widget getAdditionalWidget() {
        FlowPanel panel = new FlowPanel();
        panel.add(refreshButton);
        panel.add(courseManagementWidget);
        HorizontalPanel hp = new HorizontalPanel();
        panel.add(hp);
        hp.setSpacing(3);
        hp.add(new Label(stringMessages.authorPriority()));
        hp.add(priorityBox);
        return panel;
    }
    
    @Override
    protected Result getResult() {
        return new Result(courseManagementWidget.createWaypointPairs(),
                courseManagementWidget.getORCPerformanceCurveLegInfoByOneBasedWaypointIndex(), priorityBox.getValue());
    }
}
