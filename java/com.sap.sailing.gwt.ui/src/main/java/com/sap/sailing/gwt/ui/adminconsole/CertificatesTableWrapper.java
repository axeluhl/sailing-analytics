package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.security.ui.client.UserService;

public class CertificatesTableWrapper<S extends RefreshableSelectionModel<ORCCertificate>> extends TableWrapper<ORCCertificate, S> {

    public CertificatesTableWrapper(SailingServiceAsync sailingService, final UserService userService, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean multiSelection, boolean enablePager, int pagingSize) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager, pagingSize, new EntityIdentityComparator<ORCCertificate>() {
            @Override
            public boolean representSameEntity(ORCCertificate cert1, ORCCertificate cert2) {
                return cert1.getId().equals(cert2.getId());
            }
            @Override
            public int hashCode(ORCCertificate cert) {
                return cert.getId().hashCode();
            }
        });
    }

}
