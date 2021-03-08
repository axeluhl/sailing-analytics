package com.sap.sailing.landscape.ui.client;

import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sailing.landscape.ui.shared.RedirectDTO;
import com.sap.sailing.landscape.ui.shared.SailingApplicationReplicaSetDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class DefineLandingPageDialog extends DataEntryDialog<RedirectDTO> {

    public DefineLandingPageDialog(SailingApplicationReplicaSetDTO<String> applicationReplicaSetToDefineLandingPageFor,
            StringMessages stringMessages, ErrorReporter errorReporter,
            LandscapeManagementWriteServiceAsync landscapeManagementService,
            DialogCallback<RedirectDTO> dialogCallback) {
        super(stringMessages.scale(), /* message */ null, stringMessages.ok(), stringMessages.cancel(),
                /* validator */ null, /* animationEnabled */ true, dialogCallback);
    }

    @Override
    protected RedirectDTO getResult() {
        // TODO Implement DataEntryDialog<RedirectDTO>.getResult(...)
        return null;
    }

}
