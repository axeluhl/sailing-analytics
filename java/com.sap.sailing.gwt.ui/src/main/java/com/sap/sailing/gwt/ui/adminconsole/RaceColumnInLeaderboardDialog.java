package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

/**
 * Fills a {@link RaceColumnDTO} object by setting its {@link RaceColumnDTO#setMedalRace(boolean) medal race} property and its name.
 * @author Axel Uhl (D043530)
 *
 */
public class RaceColumnInLeaderboardDialog extends DataEntryDialog<RaceColumnDTO> {
    private final TextBox raceNameBox;
    private final CheckBox isMedalRace;
    private final StringMessages stringConstants;

    private RaceColumnDTO raceColumnToEdit;

    private static class RaceDialogValidator implements Validator<RaceColumnDTO> {

        private StringMessages stringConstants;
        private List<RaceColumnDTO> existingRaces;

        public RaceDialogValidator(StringMessages stringConstants, List<RaceColumnDTO> existingRaceColumnsAndFleetNames) {
            this.stringConstants = stringConstants;
            this.existingRaces = existingRaceColumnsAndFleetNames;
        }

        @Override
        public String getErrorMessage(RaceColumnDTO valueToValidate) {
            String errorMessage;
            String racename = valueToValidate.getRaceColumnName();
            Boolean isMedalRace = valueToValidate.isMedalRace();
            boolean isNameNotEmpty = racename != null & racename != "";
            boolean medalRaceNotNull = isMedalRace != null;

            boolean unique = true;
            for (RaceColumnDTO raceColumn : existingRaces) {
                if (raceColumn.getRaceColumnName().equals(valueToValidate.getRaceColumnName())) {
                    unique = false;
                }
            }

            if (!isNameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterAName();
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

    public RaceColumnInLeaderboardDialog(List<RaceColumnDTO> existingRaces, RaceColumnDTO raceColumnToEdit, 
            StringMessages stringConstants, AsyncCallback<RaceColumnDTO> callback) {
        super(stringConstants.name(), null, stringConstants.ok(), stringConstants.cancel(),
                new RaceDialogValidator(stringConstants, existingRaces), callback);
        this.raceColumnToEdit = raceColumnToEdit;
        this.stringConstants = stringConstants;
        raceNameBox = createTextBox(raceColumnToEdit.getRaceColumnName());
        isMedalRace = createCheckbox(stringConstants.medalRace());
        isMedalRace.setValue(raceColumnToEdit.isMedalRace());
    }

    @Override
    protected RaceColumnDTO getResult() {
        raceColumnToEdit.name = raceNameBox.getValue();
        raceColumnToEdit.setMedalRace(isMedalRace.getValue());
        return raceColumnToEdit;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = new VerticalPanel();
        
        HorizontalPanel raceNamePanel = new HorizontalPanel();
        mainPanel.add(raceNamePanel);
        raceNamePanel.add(new Label(stringConstants.name() + ":"));
        raceNamePanel.add(raceNameBox);
        
        mainPanel.add(isMedalRace);
        return mainPanel;
    }

    @Override
    public void show() {
        super.show();
        raceNameBox.setFocus(true);
    }
}
