package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.impl.AbstractLogImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
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
public class RaceLogImpl extends AbstractLogImpl<RaceLogEvent> implements RaceLog {

    private static final long serialVersionUID = 98032278604708475L;

    public RaceLogImpl(Serializable identifier) {
        super(identifier);
    }

    public RaceLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(nameForReadWriteLock, identifier);
    }
}
