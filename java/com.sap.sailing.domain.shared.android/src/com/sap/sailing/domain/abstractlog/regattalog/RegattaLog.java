package com.sap.sailing.domain.abstractlog.regattalog;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sse.common.WithID;

public interface RegattaLog extends AbstractLog<RegattaLogEvent, RegattaLogEventVisitor>, WithID {

}
