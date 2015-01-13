package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.RevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;

public interface RegattaLogRevokeEvent extends RevokeEvent<RegattaLogEventVisitor>, RegattaLogEvent {

}
