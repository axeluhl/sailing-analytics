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
        } else if (valueToValidate.getFlightMultiplier() < 0) {
            errorMessage = "Invalid flight multiplier";
        }
        
        return errorMessage;
    }

}
