package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;

public class AbstractPairingListParameterValidator implements Validator<RegattaDTO> {
    protected final StringMessages stringMessages;

    public AbstractPairingListParameterValidator(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    } 
    
    @Override
    public String getErrorMessage(RegattaDTO valueToValidate) {
        // TODO validate RegattaTDO
        return null;
    }

}
