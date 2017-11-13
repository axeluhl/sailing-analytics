package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetStartTimeDialog extends SetTimeDialog<RaceLogSetStartTimeAndProcedureDTO> {
    protected ListBox racingProcedureSelection;
    protected CheckBox advancePassIdCheckbox;

    public SetStartTimeDialog(SailingServiceAsync service, ErrorReporter errorReporter, String leaderboardName,
            String raceColumnName, String fleetName, StringMessages stringMessages,
            DataEntryDialog.DialogCallback<RaceLogSetStartTimeAndProcedureDTO> callback) {
        super(service, errorReporter, stringMessages.setStartTime(), stringMessages.setStartTimeDescription(),
                stringMessages.setStartTime(), stringMessages.cancel(), leaderboardName, raceColumnName, fleetName,
                stringMessages, new StartTimeValidator(stringMessages), callback);
        this.ensureDebugId("SetStartTimeDialog");
    }

    @Override
    protected RaceLogSetStartTimeAndProcedureDTO getResult() {
        RaceLogSetStartTimeAndProcedureDTO dto = new RaceLogSetStartTimeAndProcedureDTO();
        dto.leaderboardName = leaderboardName;
        dto.raceColumnName = raceColumnName;
        dto.fleetName = fleetName;
        dto.authorName = authorNameBox.getValue();
        dto.authorPriority = authorPriorityBox.getValue();
        dto.logicalTimePoint = new Date();
        dto.startTime = timeBox.getValue();
        dto.passId = advancePassIdCheckbox.getValue() ? currentPassId + 1 : currentPassId;
        dto.racingProcedure = RacingProcedureType.values()[racingProcedureSelection.getSelectedIndex()];
        return dto;
    }

    @Override
    protected void refreshCurrentTime() {
        service.getStartTimeAndProcedure(leaderboardName, raceColumnName, fleetName,
                new AsyncCallback<com.sap.sse.common.Util.Triple<Date, Integer, RacingProcedureType>>() {

                    @Override
                    public void onSuccess(com.sap.sse.common.Util.Triple<Date, Integer, RacingProcedureType> result) {
                        if (result == null) {
                            currentStartOrFinishingTimeLabel.setText(stringMessages.notAvailable());
                            currentPassIdBox.setText(stringMessages.notAvailable());
                        } else {
                            Date startTime = result.getA();
                            if (startTime == null) {
                                currentStartOrFinishingTimeLabel.setText(stringMessages.unknown());
                            } else {
                                currentStartOrFinishingTimeLabel.setText(DateTimeFormat
                                        .getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(startTime));
                            }
                            currentPassId = result.getB().intValue();
                            currentPassIdBox.setText(result.getB().toString());
                            int racingProcedureIndex = 0;
                            for (RacingProcedureType racingProcedureSelect : RacingProcedureType.values()) {
                                if (racingProcedureSelect.equals(result.getC())) {
                                    break;
                                }
                                racingProcedureIndex++;
                            }
                            racingProcedureSelection.setSelectedIndex(racingProcedureIndex);
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }
                });
    }

    private static class StartTimeValidator implements Validator<RaceLogSetStartTimeAndProcedureDTO> {

        private final StringMessages stringMessages;

        public StartTimeValidator(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public String getErrorMessage(RaceLogSetStartTimeAndProcedureDTO dto) {
            if (dto.authorName == null || dto.authorName.isEmpty() || dto.authorPriority == null
                    || dto.startTime == null) {
                return stringMessages.pleaseEnterAValue();
            }
            return null;
        }

    }

    @Override
    protected String getTimeLabel() {
        return stringMessages.startTime();
    }

    @Override
    protected void addAdditionalInput(Grid content) {
        racingProcedureSelection = createListBox(false);
        ListBoxUtils.setupRacingProcedureTypeListBox(racingProcedureSelection, RacingProcedureType.RRS26,
                stringMessages.no());
        int racingCounter = 0;
        for (RacingProcedureType racingType : RacingProcedureType.values()) {
            racingProcedureSelection.setValue(racingCounter++, racingType.name());
        }
        racingProcedureSelection.ensureDebugId("RacingProcedureListBox");
        content.setWidget(4, 0, createLabel(stringMessages.racingProcedure()));
        content.setWidget(4, 1, racingProcedureSelection);

        advancePassIdCheckbox = createCheckbox(stringMessages.advancePassId());
        advancePassIdCheckbox.setValue(false);
        advancePassIdCheckbox.ensureDebugId("AnvancePassIdCheckBox");
        content.setWidget(5, 0, advancePassIdCheckbox);
    }

    @Override
    protected void additionalInput(Grid content) {
    }

}
