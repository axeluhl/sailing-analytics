package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceDAO;

public class RaceDialog extends DataEntryDialog<Pair<RaceDAO, Boolean>>{

    private final TextBox raceNameBox;
    private final CheckBox isMedalRace;
    
    private Pair<RaceDAO, Boolean> raceDaoAndIsMedalRace;
    
    private static class RaceDialogValidator implements Validator<Pair<RaceDAO, Boolean>>{
        
        private StringConstants stringConstants;
        
        public RaceDialogValidator(StringConstants stringConstants) {
            this.stringConstants = stringConstants;
        }
        
        @Override
        public String getErrorMessage(Pair<RaceDAO, Boolean> valueToValidate) {
            String errorMessage;
            RaceDAO race = valueToValidate.getA();
            Boolean isMedalRace = valueToValidate.getB();
            boolean isNameNotEmpty = race!=null & race.name!=null & race.name!="";
            boolean medalRaceNotNull = isMedalRace!=null;
            
            if(!isNameNotEmpty){
                errorMessage = "error"; // stringConstants.raceNameEmpty();
            }else if(!medalRaceNotNull){
                errorMessage = "error"; //stringConstants.medalRaceIsNull();
            }else{
                return errorMessage = null;
            }
            return errorMessage;
        }
        
    }
    
    
    public RaceDialog(Pair<RaceDAO, Boolean> raceDaoAndIsMedalRace, StringConstants stringConstants,
            AsyncCallback<Pair<RaceDAO, Boolean>> callback) {
        super(stringConstants.name(), stringConstants.name(), stringConstants.ok(), stringConstants.cancel(), new RaceDialogValidator(stringConstants), callback);
        this.raceDaoAndIsMedalRace = raceDaoAndIsMedalRace;
        raceNameBox = createTextBox(raceDaoAndIsMedalRace.getA().name);
        isMedalRace = createCheckbox(stringConstants.medalRace());
        isMedalRace.setValue(raceDaoAndIsMedalRace.getB().booleanValue());
    }

    @Override
    protected Pair<RaceDAO, Boolean> getResult() {
        raceDaoAndIsMedalRace.getA().name = raceNameBox.getValue();
        raceDaoAndIsMedalRace.setB(isMedalRace.getValue()); 
        return raceDaoAndIsMedalRace;
    }

    @Override
    public void show() {
        super.show();
        raceNameBox.setFocus(true);
    }
    
    

}
