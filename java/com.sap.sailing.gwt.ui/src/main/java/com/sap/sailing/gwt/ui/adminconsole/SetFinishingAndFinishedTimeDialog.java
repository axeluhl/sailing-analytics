package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogSetFinishingAndFinishTimeDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetFinishingAndFinishedTimeDialog extends SetTimeDialog<RaceLogSetFinishingAndFinishTimeDTO> {
    private DateAndTimeInput finishTimeBox;
    private Label currentFinishTimeLabel;

    public SetFinishingAndFinishedTimeDialog(SailingServiceAsync service, ErrorReporter errorReporter, String leaderboardName,
            String raceColumnName, String fleetName, StringMessages stringMessages,
            DataEntryDialog.DialogCallback<RaceLogSetFinishingAndFinishTimeDTO> callback) {
        super(service, errorReporter, stringMessages.setFinishingAndFinishTime(), stringMessages.setFinishingAndFinishTimeDescription(),
                stringMessages.setFinishingAndFinishTime(), stringMessages.cancel(), leaderboardName, raceColumnName, fleetName,
                stringMessages, new EndTimeValidator(stringMessages), callback);
        this.ensureDebugId("SetFinishingAndFinishedTimeDialog");
    }

    @Override
    protected RaceLogSetFinishingAndFinishTimeDTO getResult() {
        RaceLogSetFinishingAndFinishTimeDTO dto = new RaceLogSetFinishingAndFinishTimeDTO();
        dto.leaderboardName = leaderboardName;
        dto.raceColumnName = raceColumnName;
        dto.fleetName = fleetName;
        dto.authorName = authorNameBox.getValue();
        dto.authorPriority = authorPriorityBox.getValue();
        dto.finishTime = finishTimeBox.getValue();
        dto.finishingTime = timeBox.getValue();
        dto.passId = currentPassId;
        return dto;
    }

    @Override
    protected void refreshCurrentTime() {
        service.getFinishingAndFinishTime(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<com.sap.sse.common.Util.Triple<Date, Date, Integer>>() {

                    @Override
                    public void onSuccess(com.sap.sse.common.Util.Triple<Date, Date, Integer> result) {
                        if (result == null) {
                            currentFinishTimeLabel.setText(stringMessages.notAvailable());
                            currentPassIdBox.setText(stringMessages.notAvailable());
                        } else {
                            setDate(result.getA(), currentStartOrFinishingTimeLabel, timeBox);
                            setDate(result.getB(), currentFinishTimeLabel, finishTimeBox);
                            currentPassId = result.getC().intValue();
                            currentPassIdBox.setText(result.getC().toString());
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                });
    }
    
    void setDate(Date date, Label currentValueLabel, DateTimeInput dateTimeBox) {
        if (date == null) {
            currentValueLabel.setText(stringMessages.unknown());
        } else {
            dateTimeBox.setValue(date);
            currentValueLabel.setText(DateTimeFormat
                    .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(date));
        }
    }

    private static class EndTimeValidator implements Validator<RaceLogSetFinishingAndFinishTimeDTO> {

        private final StringMessages stringMessages;

        public EndTimeValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(RaceLogSetFinishingAndFinishTimeDTO dto) {
            if (dto.authorName == null || dto.authorName.isEmpty() || dto.authorPriority == null
                    || dto.finishTime == null || dto.finishingTime == null) {
                return stringMessages.pleaseEnterAValue();
            }
            // both times are != null at this point
            if (dto.finishTime.before(dto.finishingTime)) {
                return stringMessages.finishTimeMustBeAtOrAfterFinishingTime();
            }
            return null;
        }
    }

    @Override
    protected String getTimeLabel() {
        return stringMessages.finishingTime();
    }
    
    @Override
    protected void additionalCurrentTimeLabel(Grid content) {
        currentFinishTimeLabel = new Label("");
        content.setWidget(1, 0, createLabel(stringMessages.finishTimeString()));
        content.setWidget(1, 1, currentFinishTimeLabel);
    }

    @Override
    protected void addAdditionalInput(Grid content) {
    }

    @Override
    protected void additionalInput(Grid content) {
        finishTimeBox = createDateTimeBox(new Date(), Accuracy.SECONDS);
        finishTimeBox.ensureDebugId("FinishTimeBox");
        content.setWidget(1, 0, createLabel(stringMessages.finishTimeString()));
        content.setWidget(1, 1, finishTimeBox);
    }
}
