package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetTrackingTimesDialog extends DataEntryDialogWithBootstrap<RaceLogSetTrackingTimesDTO> {

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private final SailingServiceAsync service;
    private final ErrorReporter errorReporter;
    private final String leaderboardName;
    private final String raceColumnName;
    private final String fleetName;
    private final StringMessages stringMessages;

    private Label currentStartLabel;
    private Label currentEndLabel;

    private BetterDateTimeBox startTimeBox;
    private BetterDateTimeBox endTimeBox;
    private TextBox authorNameBox;
    private com.sap.sse.gwt.client.controls.IntegerBox authorPriorityBox;

    protected SetTrackingTimesDialog(SailingServiceAsync service, ErrorReporter errorReporter, String leaderboardName,
            String raceColumnName, String fleetName, StringMessages stringMessages,
            DataEntryDialog.DialogCallback<RaceLogSetTrackingTimesDTO> callback) {
        super(stringMessages.setTrackingTimes(), stringMessages.setTrackingTimesDescription(), stringMessages
                .setTrackingTimes(), stringMessages.cancel(), new TrackingTimesValidator(stringMessages), callback);
        this.service = service;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.stringMessages = stringMessages;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel content = new VerticalPanel();
        content.add(createCurrentPanel());
        content.add(createInputPanel());
        refreshTimes();
        return content;
    }

    private void refreshTimes() {
        service.getTrackingTimes(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<Util.Pair<Date, Date>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error retrieving tracking times: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Pair<Date, Date> result) {
                        Date start = result.getA();
                        Date end = result.getB();
                        if (start == null) {
                            currentStartLabel.setText(stringMessages.notAvailable());
                        } else {
                            currentStartLabel.setText(start.toString());
                            startTimeBox.setValue(start);
                        }
                        if (end == null) {
                            currentEndLabel.setText(stringMessages.notAvailable());
                        } else {
                            currentEndLabel.setText(end.toString());
                            endTimeBox.setValue(end);
                        }
                        
                    }
                });
    }

    private Widget createInputPanel() {
        Grid content = new Grid(4, 2);
        

        startTimeBox = createDateTimeBox(null);
        startTimeBox.setFormat("dd/mm/yyyy hh:ii:ss");
        content.setWidget(0, 0, createLabel(stringMessages.startOfTracking()));
        content.setWidget(0, 1, startTimeBox);

        endTimeBox = createDateTimeBox(null);
        endTimeBox.setFormat("dd/mm/yyyy hh:ii:ss");
        content.setWidget(1, 0, createLabel(stringMessages.endOfTracking()));
        content.setWidget(1, 1, endTimeBox);

        authorNameBox = createTextBox("Shore");
        content.setWidget(2, 0, createLabel(stringMessages.authorName()));
        content.setWidget(2, 1, authorNameBox);
        authorPriorityBox = createIntegerBox(4, 2);
        content.setWidget(3, 0, createLabel(stringMessages.authorPriority()));
        content.setWidget(3, 1, authorPriorityBox);

        return content;
    }

    private Widget createCurrentPanel() {
        CaptionPanel current = new CaptionPanel(stringMessages.liveData());
        HorizontalPanel currentPanel = new HorizontalPanel();
        currentPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        Grid grid = new Grid(2, 2);
        currentStartLabel = new Label("");
        grid.setWidget(0, 0, createLabel(stringMessages.startOfTracking()));
        grid.setWidget(0, 1, currentStartLabel);

        currentEndLabel = new Label("");
        grid.setWidget(1, 0, createLabel(stringMessages.endOfTracking()));
        grid.setWidget(1, 1, currentEndLabel);
        currentPanel.add(grid);

        PushButton refreshButton = new PushButton(new Image(resources.reloadIcon()));
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshTimes();
            }
        });
        currentPanel.add(refreshButton);

        current.add(currentPanel);
        return current;
    }

    @Override
    protected RaceLogSetTrackingTimesDTO getResult() {
        RaceLogSetTrackingTimesDTO dto = new RaceLogSetTrackingTimesDTO();
        dto.leaderboardName = leaderboardName;
        dto.raceColumnName = raceColumnName;
        dto.fleetName = fleetName;
        dto.authorName = authorNameBox.getValue();
        dto.authorPriority = authorPriorityBox.getValue();
        dto.logicalTimePoint = new Date();
        dto.endOfTracking = endTimeBox.getValue();
        dto.startOfTracking = startTimeBox.getValue();
        return dto;
    }

    private static class TrackingTimesValidator implements Validator<RaceLogSetTrackingTimesDTO> {

        private final StringMessages stringMessages;

        public TrackingTimesValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(RaceLogSetTrackingTimesDTO dto) {
            if (dto.authorName == null || dto.authorPriority == null
                    || (dto.startOfTracking == null && dto.endOfTracking == null)) {
                return stringMessages.pleaseEnterAValue();
            }
            return null;
        }

    }

}
