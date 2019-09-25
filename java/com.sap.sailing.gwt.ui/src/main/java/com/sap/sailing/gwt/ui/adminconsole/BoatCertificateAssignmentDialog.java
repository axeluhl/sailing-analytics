package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.ui.client.UserService;

public class BoatCertificateAssignmentDialog extends DataEntryDialog<List<BoatDTO>> {
    /*
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;
    private final String regattaName;
    */
    private final BoatCertificatesPanel panel;

    protected static class BoatValidator implements Validator<List<BoatDTO>> {
        public BoatValidator() {
            super();
        }

        @Override
        public String getErrorMessage(List<BoatDTO> valueToValidate) {
            return null;
        }
    }
        
    public BoatCertificateAssignmentDialog(final SailingServiceAsync sailingService, UserService userService, String regattaName, final StringMessages stringMessages,
            final ErrorReporter errorReporter, DialogCallback<List<BoatDTO>> callback) {
        super(stringMessages.actionEditCompetitors(), stringMessages.actionEditCompetitors(), stringMessages.save(), stringMessages.cancel(), new BoatValidator(), callback);
        /*
        this.sailingService = sailingService;
        this.userService = userService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaName = regattaName;
        */
        
        this.panel = new BoatCertificatesPanel(sailingService, userService, regattaName, stringMessages, errorReporter);
    }

    @Override
    protected List<BoatDTO> getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        return panel; 
    }
}
