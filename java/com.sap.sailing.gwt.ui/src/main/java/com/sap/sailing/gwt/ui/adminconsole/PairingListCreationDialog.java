package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class PairingListCreationDialog extends AbstractPairingListCreationDialog<RegattaDTO> {
    
    protected static class PairingListParameterValidator extends AbstractPairingListParameterValidator {
        public PairingListParameterValidator(StringMessages stringMessages) {
            super(stringMessages);
        }
    }

    public PairingListCreationDialog(RegattaIdentifier regattaIdentifier, final StringMessages stringMessages,
            DialogCallback<RegattaDTO> callback) {
        // TODO stringMessages 
        super(regattaIdentifier, stringMessages.pairingLists(), stringMessages, new PairingListParameterValidator(stringMessages), 
                callback);
        
        this.ensureDebugId("PairingListCreationDialog");
        
        
    }
    
}
