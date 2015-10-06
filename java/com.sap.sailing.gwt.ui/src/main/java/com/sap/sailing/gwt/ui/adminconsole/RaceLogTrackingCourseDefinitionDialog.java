package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RaceLogTrackingCourseDefinitionDialog extends
        DataEntryDialog<List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>>> {

    private CourseManagementWidget courseManagementWidget;
    private Button refreshButton;

    public RaceLogTrackingCourseDefinitionDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, final String leaderboardName,
            final String raceColumnName, final String fleetName,
            DialogCallback<List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>>> callback) {

        super(stringMessages.defineCourse(), null, stringMessages.save(), stringMessages.cancel(), /* validator */null,
                callback);

        courseManagementWidget = new RaceLogCourseManagementWidget(sailingService, errorReporter, stringMessages,
                leaderboardName, raceColumnName, fleetName);
        
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
        return panel;
    }
    @Override
    protected List<Pair<ControlPointDTO, PassingInstruction>> getResult() {
        return courseManagementWidget.createWaypointPairs();
    }
}
