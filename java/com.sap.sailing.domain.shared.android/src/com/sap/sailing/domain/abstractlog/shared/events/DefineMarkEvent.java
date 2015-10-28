package com.sap.sailing.domain.abstractlog.shared.events;

import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.base.Mark;

public interface DefineMarkEvent<VisitorT> extends AbstractLogEvent<VisitorT>, Revokable {
    Mark getMark();
}
