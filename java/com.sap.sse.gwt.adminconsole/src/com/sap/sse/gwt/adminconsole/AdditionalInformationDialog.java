package com.sap.sse.gwt.adminconsole;

import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class AdditionalInformationDialog extends DataEntryDialog<String>{

    public AdditionalInformationDialog(String title, String message, String okButtonName, String cancelButtonName,
            Validator<String> validator, boolean animationEnabled, DialogCallback<String> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, animationEnabled, callback);
    }

    @Override
    protected String getResult() {
        return null;
    }
    
}
