package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PairingListCreationDialog extends AbstractPairingListCreationDialog<PairingListTemplateDTO> {
    
    protected static class PairingListParameterValidator extends AbstractPairingListParameterValidator {
        public PairingListParameterValidator(StringMessages stringMessages) {
            super(stringMessages);
        }
    }

    public PairingListCreationDialog(RegattaIdentifier regattaIdentifier, final StringMessages stringMessages, 
            DialogCallback<PairingListTemplateDTO> callback) {
        // TODO stringMessages 
        super(regattaIdentifier, stringMessages.pairingLists(), stringMessages, new PairingListParameterValidator(stringMessages), 
                callback);
        
        this.ensureDebugId("PairingListCreationDialog");
    }
    
}
