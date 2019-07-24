package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;

// TODO Here is needed a solution/concept to use this kind of events in race and regatta logs.
// Additionally the logic shouldn't be implemented at two different places.
public interface ORCCertificateUpdateEvent /* extends AbstractLogEvent<VisitorT> */ {

}
