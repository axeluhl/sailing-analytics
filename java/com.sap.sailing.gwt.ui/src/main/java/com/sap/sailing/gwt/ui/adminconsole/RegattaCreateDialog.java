package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaCreateDialog extends RegattaDialog {

    public RegattaCreateDialog(Collection<RegattaDTO> existingRegattas,
            StringMessages stringConstants, AsyncCallback<RegattaDTO> callback) {
        super(new RegattaDTO(), new RegattaParameterValidator(stringConstants, existingRegattas),
                stringConstants, callback);

        nameEntryField = createTextBox(null);
        nameEntryField.setWidth("200px");
        boatClassEntryField = createTextBox(null);
        boatClassEntryField.setWidth("200px");
    }

}
