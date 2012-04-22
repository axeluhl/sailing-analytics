package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;

public class RaceInLeaderboardDialog extends DataEntryDialog<RaceInLeaderboardDTO> {

    private final TextBox raceNameBox;
    private final CheckBox isMedalRace;

    private RaceInLeaderboardDTO raceInLeaderboard;

    private static class RaceDialogValidator implements Validator<RaceInLeaderboardDTO> {

        private StringMessages stringConstants;
        private Collection<RaceInLeaderboardDTO> existingRaces;

        public RaceDialogValidator(StringMessages stringConstants, Collection<RaceInLeaderboardDTO> existingRaces) {
            this.stringConstants = stringConstants;
            this.existingRaces = existingRaces;
        }

        @Override
        public String getErrorMessage(RaceInLeaderboardDTO valueToValidate) {
            String errorMessage;
            String racename = valueToValidate.getRaceColumnName();
            Boolean isMedalRace = valueToValidate.isMedalRace();
            boolean isNameNotEmpty = racename != null & racename != "";
            boolean medalRaceNotNull = isMedalRace != null;

            boolean unique = true;
            for (RaceInLeaderboardDTO dao : existingRaces) {
                if (dao.getRaceColumnName().equals(valueToValidate.getRaceColumnName())) {
                    unique = false;
                }
            }

            if (!isNameNotEmpty) {
                errorMessage = stringConstants.raceNameEmpty();
            } else if (!medalRaceNotNull) {
                errorMessage = stringConstants.medalRaceIsNull();
            } else if (!unique) {
                errorMessage = stringConstants.raceWithThisNameAlreadyExists();
            } else {
                return errorMessage = null;
            }
            return errorMessage;
        }

    }

    public RaceInLeaderboardDialog(Collection<RaceInLeaderboardDTO> existingRaces,
            RaceInLeaderboardDTO raceInLeaderboard, StringMessages stringConstants,
            AsyncCallback<RaceInLeaderboardDTO> callback) {
        super(stringConstants.name(), null, stringConstants.ok(), stringConstants.cancel(),
                new RaceDialogValidator(stringConstants, existingRaces), callback);
        this.raceInLeaderboard = raceInLeaderboard;
        raceNameBox = createTextBox(raceInLeaderboard.getRaceColumnName());
        isMedalRace = createCheckbox(stringConstants.medalRace());
        isMedalRace.setValue(raceInLeaderboard.isMedalRace());
    }

    @Override
    protected RaceInLeaderboardDTO getResult() {
        raceInLeaderboard.setRaceColumnName(raceNameBox.getValue());
        raceInLeaderboard.setMedalRace(isMedalRace.getValue());
        return raceInLeaderboard;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        panel.add(raceNameBox);
        panel.add(isMedalRace);
        return panel;
    }

    @Override
    public void show() {
        super.show();
        raceNameBox.setFocus(true);
    }

}
