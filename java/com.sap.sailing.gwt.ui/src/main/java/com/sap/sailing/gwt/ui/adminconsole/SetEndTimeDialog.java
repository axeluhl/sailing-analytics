package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.RaceLogSetFinishingAndEndTimeDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetEndTimeDialog extends SetTimeDialog<RaceLogSetFinishingAndEndTimeDTO> {
    private BetterDateTimeBox finishTimeBox;
    private Label currentEndTimeLabel;

    public SetEndTimeDialog(SailingServiceAsync service, ErrorReporter errorReporter, String leaderboardName,
            String raceColumnName, String fleetName, StringMessages stringMessages,
            DataEntryDialog.DialogCallback<RaceLogSetFinishingAndEndTimeDTO> callback) {
        super(service, errorReporter, stringMessages.setFinishingAndEndTime(), stringMessages.setFinishingAndEndTimeDescription(),
                stringMessages.setFinishingAndEndTime(), stringMessages.cancel(), leaderboardName, raceColumnName, fleetName,
                stringMessages, new EndTimeValidator(stringMessages), callback);
        this.ensureDebugId("SetEndTimeDialog");
    }

    @Override
    protected RaceLogSetFinishingAndEndTimeDTO getResult() {
        RaceLogSetFinishingAndEndTimeDTO dto = new RaceLogSetFinishingAndEndTimeDTO();
        dto.leaderboardName = leaderboardName;
        dto.raceColumnName = raceColumnName;
        dto.fleetName = fleetName;
        dto.authorName = authorNameBox.getValue();
        dto.authorPriority = authorPriorityBox.getValue();
        dto.finishTime = finishTimeBox.getValue();
        dto.finishingTime = timeBox.getValue();
        dto.passId = advancePassIdCheckbox.getValue() ? currentPassId + 1 : currentPassId;
        return dto;
    }

    @Override
    protected void refreshCurrentTime() {
        service.getEndTime(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<com.sap.sse.common.Util.Pair<Date, Integer>>() {

                    @Override
                    public void onSuccess(com.sap.sse.common.Util.Pair<Date, Integer> result) {
                        if (result == null) {
                            currentEndTimeLabel.setText(stringMessages.notAvailable());
                            currentPassIdBox.setText(stringMessages.notAvailable());
                        } else {
                            Date startTime = result.getA();
                            if (startTime == null) {
                                currentEndTimeLabel.setText(stringMessages.unknown());
                            } else {
                                finishTimeBox.setValue(startTime);
                                currentEndTimeLabel.setText(DateTimeFormat
                                        .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(startTime));
                            }
                            currentPassId = result.getB().intValue();
                            currentPassIdBox.setText(result.getB().toString());
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                });
        
        service.getFinishingTime(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<com.sap.sse.common.Util.Pair<Date, Integer>>() {

                    @Override
                    public void onSuccess(com.sap.sse.common.Util.Pair<Date, Integer> result) {
                        if (result == null) {
                            currentStartOrEndTimeLabel.setText(stringMessages.notAvailable());
                            currentPassIdBox.setText(stringMessages.notAvailable());
                        } else {
                            Date startTime = result.getA();
                            if (startTime == null) {
                                currentStartOrEndTimeLabel.setText(stringMessages.unknown());
                            } else {
                                timeBox.setValue(startTime);
                                currentStartOrEndTimeLabel.setText(DateTimeFormat
                                        .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(startTime));
                            }
                            currentPassId = result.getB().intValue();
                            currentPassIdBox.setText(result.getB().toString());
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                });
    }

    private static class EndTimeValidator implements Validator<RaceLogSetFinishingAndEndTimeDTO> {

        private final StringMessages stringMessages;

        public EndTimeValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(RaceLogSetFinishingAndEndTimeDTO dto) {
            if (dto.authorName == null || dto.authorName.isEmpty() || dto.authorPriority == null
                    || dto.finishTime == null || dto.finishingTime == null) {
                return stringMessages.pleaseEnterAValue();
            }
            // both times are != null at this point
            if (dto.finishTime.before(dto.finishingTime)) {
                return stringMessages.endTimeMustBeAtOrAfterFinishingTime();
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
        currentEndTimeLabel = new Label("");
        content.setWidget(1, 0, createLabel(stringMessages.endTime()));
        content.setWidget(1, 1, currentEndTimeLabel);
    }

    @Override
    protected void addAdditionalInput(Grid content) {
    }

    @Override
    protected void additionalInput(Grid content) {
        finishTimeBox = createDateTimeBox(new Date());
        finishTimeBox.setFormat("dd/mm/yyyy hh:ii:ss");
        finishTimeBox.ensureDebugId("FinishTimeBox");
        content.setWidget(1, 0, createLabel(stringMessages.endTime()));
        content.setWidget(1, 1, finishTimeBox);
    }
}
