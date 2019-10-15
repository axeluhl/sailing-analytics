package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class ORCCertificateDialog extends DataEntryDialog<ORCCertificate> {
    //TODO Daniel: Implement ORCCertificateDialog
    
    Widget panel;

    public ORCCertificateDialog(String title, String message, String okButtonName, 
            Validator<ORCCertificate> validator, boolean animationEnabled) {
        super(title, message, okButtonName, null, validator, animationEnabled, null);
        this.panel = null;
    }

    @Override
    protected ORCCertificate getResult() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        return null;
    }
}
