package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;

/**
 * This panel houses the functionality to manage the {@link ORCCertificate} linking to the corresponding {@link Boat}.
 * Additionally the BoatTable shows some basic information about the participating Boats and the CertificateTable displays
 * the GPH, Issue Date and identification information. To upload or import some certificates, there is an UploadForm.
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (d043530)
 *
 */
public class RegattaBoatCertificatesPanel extends AbstractBoatCertificatesPanel {
    private final RegattaIdentifier regattaIdentifier;
    
    public RegattaBoatCertificatesPanel(final SailingServiceAsync sailingService, final UserService userService, final RegattaDTO regatta,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super(sailingService, userService, /* objectToCheckUpdatePermissionFor */ regatta, stringMessages, errorReporter,
                /* context update permission check: */ ()->userService.hasPermission(regatta, DefaultActions.UPDATE), regatta.getName());
        this.regattaIdentifier = regatta.getRegattaIdentifier();
    }

    @Override
    protected void assignCertificates(SailingServiceAsync sailingService,
            Map<String, ORCCertificate> certificatesByBoatIdAsString,
            AsyncCallback<Triple<Integer, Integer, Integer>> callback) {
        sailingService.assignORCPerformanceCurveCertificates(regattaIdentifier, certificatesByBoatIdAsString, callback);        
    }

    protected void getORCCertificateAssignemtnsByBoatIdAsString(SailingServiceAsync sailingService,
            final AsyncCallback<Map<String, ORCCertificate>> callbackForGetCertificates) {
        sailingService.getORCCertificateAssignmentsByBoatIdAsString(regattaIdentifier, callbackForGetCertificates);
    }

    protected void getBoats(SailingServiceAsync sailingService, final AsyncCallback<Collection<BoatDTO>> callbackForGetBoats) {
        sailingService.getBoatRegistrationsForRegatta(regattaIdentifier, callbackForGetBoats);
    }
}
