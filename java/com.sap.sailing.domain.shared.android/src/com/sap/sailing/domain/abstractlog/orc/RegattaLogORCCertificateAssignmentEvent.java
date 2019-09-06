package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;

public interface RegattaLogORCCertificateAssignmentEvent extends ORCCertificateAssignmentEvent<RegattaLogEventVisitor>, RegattaLogEvent {
}
