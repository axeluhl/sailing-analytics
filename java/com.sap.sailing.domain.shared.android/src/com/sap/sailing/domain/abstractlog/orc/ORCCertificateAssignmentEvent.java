package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorHandicapInfoEvent;
import com.sap.sailing.domain.common.orc.ORCCertificate;

// TODO Here is needed a solution/concept to use this kind of events in race and regatta logs.
// Additionally the logic shouldn't be implemented at two different places.
public interface ORCCertificateAssignmentEvent extends RegattaLogSetCompetitorHandicapInfoEvent {

    public ORCCertificate getCertificate();
    
}
