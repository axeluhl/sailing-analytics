package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;

/**
 * Fills a {@link RaceColumnDTO} object by setting its {@link RaceColumnDTO#setMedalRace(boolean) medal race} property and its name.
 * As {@link RaceColumnDTO#getFleetNames()} fleet, a single default fleet is added.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceColumnInLeaderboardDialog extends DataEntryDialog<Pair<RaceColumnDTO, String>> {
    private final static String DEFAULT_FLEET_NAME = "Default";
    
    private final TextBox raceNameBox;
    private final CheckBox isMedalRace;

    private RaceColumnDTO raceInLeaderboard;

    private static class RaceDialogValidator implements Validator<Pair<RaceColumnDTO, String>> {

        private StringMessages stringConstants;
        private Collection<Pair<RaceColumnDTO, String>> existingRaces;

        public RaceDialogValidator(StringMessages stringConstants, Collection<Pair<RaceColumnDTO, String>> existingRaceColumnsAndFleetNames) {
            this.stringConstants = stringConstants;
            this.existingRaces = existingRaceColumnsAndFleetNames;
        }

        @Override
        public String getErrorMessage(Pair<RaceColumnDTO, String> valueToValidate) {
            String errorMessage;
            String racename = valueToValidate.getA().getRaceColumnName();
            Boolean isMedalRace = valueToValidate.getA().isMedalRace();
            boolean isNameNotEmpty = racename != null & racename != "";
            boolean medalRaceNotNull = isMedalRace != null;

            boolean unique = true;
            for (Pair<RaceColumnDTO, String> raceColumnAndFleetName : existingRaces) {
                if (raceColumnAndFleetName.getA().getRaceColumnName().equals(valueToValidate.getA().getRaceColumnName())) {
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

    public RaceColumnInLeaderboardDialog(Collection<Pair<RaceColumnDTO, String>> existingRaces,
            RaceColumnDTO raceInLeaderboard, StringMessages stringConstants,
            AsyncCallback<Pair<RaceColumnDTO, String>> callback) {
        super(stringConstants.name(), null, stringConstants.ok(), stringConstants.cancel(),
                new RaceDialogValidator(stringConstants, existingRaces), callback);
        this.raceInLeaderboard = raceInLeaderboard;
        raceNameBox = createTextBox(raceInLeaderboard.getRaceColumnName());
        isMedalRace = createCheckbox(stringConstants.medalRace());
        isMedalRace.setValue(raceInLeaderboard.isMedalRace());
    }

    @Override
    protected Pair<RaceColumnDTO, String> getResult() {
        raceInLeaderboard.name = raceNameBox.getValue();
        raceInLeaderboard.setMedalRace(isMedalRace.getValue());
        raceInLeaderboard.addFleetName(DEFAULT_FLEET_NAME);
        return new Pair<RaceColumnDTO, String>(raceInLeaderboard, raceInLeaderboard.getFleetNames().iterator().next());
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
