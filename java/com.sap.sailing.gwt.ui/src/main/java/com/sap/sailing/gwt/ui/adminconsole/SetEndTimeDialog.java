package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogSetEndTimeDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetEndTimeDialog extends SetTimeDialog<RaceLogSetEndTimeDTO> {
    public SetEndTimeDialog(SailingServiceAsync service, ErrorReporter errorReporter, String leaderboardName,
            String raceColumnName, String fleetName, StringMessages stringMessages,
            DataEntryDialog.DialogCallback<RaceLogSetEndTimeDTO> callback) {
        super(service, errorReporter, stringMessages.setEndTime(), stringMessages.setEndTimeDescription(),
                stringMessages.setEndTime(), stringMessages.cancel(), leaderboardName, raceColumnName, fleetName,
                stringMessages, new EndTimeValidator(stringMessages), callback);
        this.ensureDebugId("SetEndTimeDialog");
    }

    @Override
    protected RaceLogSetEndTimeDTO getResult() {
        RaceLogSetEndTimeDTO dto = new RaceLogSetEndTimeDTO();
        dto.leaderboardName = leaderboardName;
        dto.raceColumnName = raceColumnName;
        dto.fleetName = fleetName;
        dto.authorName = authorNameBox.getValue();
        dto.authorPriority = authorPriorityBox.getValue();
        dto.logicalTimePoint = new Date();
        dto.endTime = timeBox.getValue();
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
                            currentStartOrEndTimeLabel.setText(stringMessages.notAvailable());
                            currentPassIdBox.setText(stringMessages.notAvailable());
                        } else {
                            Date startTime = result.getA();
                            if (startTime == null) {
                                currentStartOrEndTimeLabel.setText(stringMessages.unknown());
                            } else {
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

    private static class EndTimeValidator implements Validator<RaceLogSetEndTimeDTO> {

        private final StringMessages stringMessages;

        public EndTimeValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(RaceLogSetEndTimeDTO dto) {
            if (dto.authorName == null || dto.authorName.isEmpty() || dto.authorPriority == null
                    || dto.endTime == null) {
                return stringMessages.pleaseEnterAValue();
            }
            return null;
        }

    }

    @Override
    protected String getTimeLabel() {
        return stringMessages.setEndTime();
    }

    @Override
    protected void addAdditionalInput(Grid content) {
    }

}
