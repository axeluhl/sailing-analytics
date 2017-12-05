package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class AbstractPairingListParameterValidator implements Validator<PairingListTemplateDTO> {
    protected final StringMessages stringMessages;

    public AbstractPairingListParameterValidator(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    } 
    
    @Override
    public String getErrorMessage(PairingListTemplateDTO valueToValidate) {
        String errorMessage = null;
        
        if (valueToValidate.getCompetitorCount() < valueToValidate.getGroupCount()) {
            errorMessage = "Invalid competitor count";
        } else if (valueToValidate.getFlightMultiplier() < 1) {
            errorMessage = "Invalid flight multiplier";
        } else if(valueToValidate.getFlightCount()%valueToValidate.getFlightMultiplier()!=0 &&valueToValidate.getFlightMultiplier()>0){
            errorMessage= "Flightcount has to be a multiple of Flightmultiplier!";
        }
        
        return errorMessage;
    }

}
