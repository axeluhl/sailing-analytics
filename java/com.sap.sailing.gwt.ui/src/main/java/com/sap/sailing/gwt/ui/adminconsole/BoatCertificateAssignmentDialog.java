package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

/**
 * Shows an {@link AbstractBoatCertificatesPanel} in a dialog. The panel interacts with a servlet that receives uploaded
 * certificate files, certificate download URLs, as well as certificate assignment specifications and returns a JSON
 * object containing the certificates found and the assignment results. This dialog produces its own callback. When
 * the dialog is confirmed using its "save" button, the {@link AbstractBoatCertificatesPanel#assignCertificates()} method
 * is invoked, making the changes permanent. When canceled, all changes are discarded.
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (D043530)
 *
 */
public class BoatCertificateAssignmentDialog extends DataEntryDialog<Void> {
    
    private final AbstractBoatCertificatesPanel panel;
    
    private static class Callback implements DialogCallback<Void> {
        private final AbstractBoatCertificatesPanel boatCertificatesPanel;

        public Callback(AbstractBoatCertificatesPanel regattaBoatCertificatesPanel) {
            this.boatCertificatesPanel = regattaBoatCertificatesPanel;
        }

        @Override
        public void ok(Void editedObject) {
            boatCertificatesPanel.assignCertificates();
        }

        @Override
        public void cancel() {
        }
        
    }

    public BoatCertificateAssignmentDialog(final SailingServiceAsync sailingService, UserService userService,
            final StringMessages stringMessages, final ErrorReporter errorReporter,
            AbstractBoatCertificatesPanel regattaBoatCertificatesPanel) {
        super(stringMessages.actionEditCompetitors(), stringMessages.actionEditCompetitors(), stringMessages.save(),
                stringMessages.cancel(), /* validator */ null, new Callback(regattaBoatCertificatesPanel));
        this.panel = regattaBoatCertificatesPanel;
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
