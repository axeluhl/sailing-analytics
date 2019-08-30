package com.sap.sailing.domain.abstractlog.orc;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.common.orc.ORCCertificate;

// TODO Here is needed a solution/concept to use this kind of events in race and regatta logs.
// Additionally the logic shouldn't be implemented at two different places.
public interface ORCCertificateAssignmentEvent extends RegattaLogEvent {

    public Serializable getCompetitorID();
    
    public ORCCertificate getCertificate();
    
}
