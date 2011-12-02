package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDAO;

public class RaceDialog extends DataEntryDialog<RaceInLeaderboardDAO>{

    private final TextBox raceNameBox;
    private final CheckBox isMedalRace;
    
    private RaceInLeaderboardDAO raceInLeaderboard;
    
    private static class RaceDialogValidator implements Validator<RaceInLeaderboardDAO>{
        
        private StringConstants stringConstants;
        
        public RaceDialogValidator(StringConstants stringConstants) {
            this.stringConstants = stringConstants;
        }
        
        @Override
        public String getErrorMessage(RaceInLeaderboardDAO valueToValidate) {
            String errorMessage;
            String racename = valueToValidate.getRaceColumnName();
            Boolean isMedalRace = valueToValidate.isMedalRace();
            boolean isNameNotEmpty =racename!=null & racename!="";
            boolean medalRaceNotNull = isMedalRace!=null;
            
            if(!isNameNotEmpty){
                errorMessage = stringConstants.raceNameEmpty();
            }else if(!medalRaceNotNull){
                errorMessage = stringConstants.medalRaceIsNull();
            }else{
                return errorMessage = null;
            }
            return errorMessage;
        }
        
    }
    
    
    public RaceDialog(RaceInLeaderboardDAO raceInLeaderboard, StringConstants stringConstants,
            AsyncCallback<RaceInLeaderboardDAO> callback) {
        super(stringConstants.name(), stringConstants.name(), stringConstants.ok(), stringConstants.cancel(), new RaceDialogValidator(stringConstants), callback);
        this.raceInLeaderboard = raceInLeaderboard;
        raceNameBox = createTextBox(raceInLeaderboard.getRaceColumnName());
        isMedalRace = createCheckbox(stringConstants.medalRace());
        isMedalRace.setValue(raceInLeaderboard.isMedalRace());
    }

    @Override
    protected RaceInLeaderboardDAO getResult() {
        raceInLeaderboard.setRaceColumnName(raceNameBox.getName());
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
