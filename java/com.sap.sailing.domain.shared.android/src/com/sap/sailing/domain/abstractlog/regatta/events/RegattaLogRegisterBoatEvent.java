package com.sap.sailing.domain.abstractlog.regatta.events;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.shared.events.RegisterBoatEvent;

public interface RegattaLogRegisterBoatEvent extends RegattaLogEvent,
RegisterBoatEvent<RegattaLogEventVisitor> {

}
