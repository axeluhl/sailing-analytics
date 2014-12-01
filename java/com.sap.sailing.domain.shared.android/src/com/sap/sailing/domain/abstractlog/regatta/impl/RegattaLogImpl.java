package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventComparator;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;

public class RegattaLogImpl extends AbstractLogImpl<RegattaLogEvent, RegattaLogEventVisitor> implements RegattaLog {

    private static final long serialVersionUID = 98032278604708475L;

    public RegattaLogImpl(Serializable identifier) {
        super(identifier, new LogEventComparator());
    }

    public RegattaLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(nameForReadWriteLock, identifier, new LogEventComparator());
    }

    @Override
    protected RegattaLogEvent createRevokeEvent(AbstractLogEventAuthor author, RegattaLogEvent toRevoke, String reason) {
        return new RegattaLogRevokeEventImpl(author, toRevoke, reason);
    }
}
