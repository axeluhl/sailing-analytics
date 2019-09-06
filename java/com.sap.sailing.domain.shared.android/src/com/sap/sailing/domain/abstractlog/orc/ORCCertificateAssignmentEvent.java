package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.common.orc.ORCCertificate;

public interface ORCCertificateAssignmentEvent<VisitorT> extends AbstractLogEvent<VisitorT> {
    Serializable getCompetitorID();
    
    ORCCertificate getCertificate();
}
