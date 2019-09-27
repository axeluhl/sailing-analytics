package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * Shows a {@link BoatCertificatesPanel} in a dialog. The panel interacts with a servlet that receives uploaded
 * certificate files, certificate download URLs, as well as certificate assignment specifications and returns a JSON
 * object containing the certificates found and the assignment results. This dialog therefore has no result.
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (D043530)
 *
 */
public class BoatCertificateAssignmentDialog extends DataEntryDialog<Void> {
    
    private final BoatCertificatesPanel panel;

    public BoatCertificateAssignmentDialog(final SailingServiceAsync sailingService, UserService userService, String regattaName, final StringMessages stringMessages,
            final ErrorReporter errorReporter, DialogCallback<Void> callback) {
        super(stringMessages.actionEditCompetitors(), stringMessages.actionEditCompetitors(), stringMessages.close(), stringMessages.cancel(), /* validator */ null, callback);
        this.panel = new BoatCertificatesPanel(sailingService, userService, regattaName, stringMessages, errorReporter);
        super.getCancelButton().removeFromParent();
    }

    @Override
    protected Void getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        return panel; 
    }
}
