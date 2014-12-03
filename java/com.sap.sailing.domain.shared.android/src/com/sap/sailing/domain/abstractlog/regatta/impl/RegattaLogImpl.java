package com.sap.sailing.domain.abstractlog.regatta.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogEventComparator;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventComparator;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventFactory;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.impl.TrackImpl;

/**
 * {@link Track} implementation for {@link RaceLogEvent}s.
 * 
 * <p>
 * "Fix" validity is decided based on the {@link #getCurrentPassId() current pass}. The validity is not cached.
 * </p>
 * 
 * <p>
 * {@link TrackImpl#getDummyFix(com.sap.sailing.domain.common.TimePoint)} is not overridden, see
 * {@link RaceLogEventComparator} for sorting when interface methods like
 * {@link Track#getFirstFixAfter(com.sap.sailing.domain.common.TimePoint)} are used.
 * </p>
 * 
 */
public class RegattaLogImpl extends AbstractLogImpl<RegattaLogEvent, RegattaLogEventVisitor> implements RegattaLog {

    private static final long serialVersionUID = 98032278604708475L;

    public RegattaLogImpl(Serializable identifier) {
        super(identifier, new AbstractLogEventComparator());
    }

    public RegattaLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(nameForReadWriteLock, identifier, new AbstractLogEventComparator());
    }

    @Override
    protected RegattaLogEvent createRevokeEvent(AbstractLogEventAuthor author, RegattaLogEvent toRevoke, String reason) {
        return RegattaLogEventFactory.INSTANCE.createRevokeEvent(author, toRevoke, reason);
    }
}
