package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.Pair;

public class RaceDialog extends DataEntryDialog<Pair<String, Boolean>>{

    private final TextBox raceNameBox;
    private final CheckBox isMedalRace;
    
    private final StringConstants stringConstants;
    
    private Pair<String, Boolean> raceDaoAndIsMedalRace;
    
    private static class RaceDialogValidator implements Validator<Pair<String, Boolean>>{
        
        private StringConstants stringConstants;
        
        public RaceDialogValidator(StringConstants stringConstants) {
            this.stringConstants = stringConstants;
        }
        
        @Override
        public String getErrorMessage(Pair<String, Boolean> valueToValidate) {
            String errorMessage;
            String racename = valueToValidate.getA();
            Boolean isMedalRace = valueToValidate.getB();
            boolean isNameNotEmpty =racename!=null & racename!="";
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
    
    
    public RaceDialog(Pair<String, Boolean> raceDaoAndIsMedalRace, StringConstants stringConstants,
            AsyncCallback<Pair<String, Boolean>> callback) {
        super(stringConstants.name(), stringConstants.name(), stringConstants.ok(), stringConstants.cancel(), new RaceDialogValidator(stringConstants), callback);
        this.raceDaoAndIsMedalRace = raceDaoAndIsMedalRace;
        raceNameBox = createTextBox(raceDaoAndIsMedalRace.getA());
        isMedalRace = createCheckbox(stringConstants.medalRace());
        isMedalRace.setValue(raceDaoAndIsMedalRace.getB().booleanValue());
        this.stringConstants = stringConstants;
    }

    @Override
    protected Pair<String, Boolean> getResult() {
        raceDaoAndIsMedalRace.setA(raceNameBox.getValue());
        raceDaoAndIsMedalRace.setB(isMedalRace.getValue()); 
        return raceDaoAndIsMedalRace;
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
