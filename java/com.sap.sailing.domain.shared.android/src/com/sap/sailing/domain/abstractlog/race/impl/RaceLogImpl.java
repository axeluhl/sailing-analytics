package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.NavigableSet;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AbstractLogImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.impl.PartialNavigableSetView;
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
public class RaceLogImpl extends AbstractLogImpl<RaceLogEvent, RaceLogEventVisitor> implements RaceLog {
    private static final long serialVersionUID = 98032278604708475L;

    public RaceLogImpl(Serializable identifier) {
        super(identifier, new RaceLogEventComparator());
    }

    public RaceLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(nameForReadWriteLock, identifier, new RaceLogEventComparator());
    }

    @Override
    public int getCurrentPassId() {
        //return pass id of last event, as pass is the top-level sorting criterion in RaceLogeventComparator
        if (! getUnrevokedEvents().isEmpty()) {
            return getUnrevokedEvents().last().getPassId();
        } else {
            return DefaultPassId;
        }
    }
    
    @Override
    protected void onSuccessfulAdd(RaceLogEvent event, boolean notifyListeners) {
        super.onSuccessfulAdd(event, notifyListeners);
    }
    
    @Override
    protected RaceLogEvent createRevokeEvent(AbstractLogEventAuthor author, RaceLogEvent toRevoke, String reason) {
        return RaceLogEventFactory.INSTANCE.createRevokeEvent(author, getCurrentPassId(), toRevoke, reason);
    }

    @Override
    protected NavigableSet<RaceLogEvent> getInternalFixes() {
        return new PartialNavigableSetView<RaceLogEvent>(super.getInternalFixes()) {
            @Override
            protected boolean isValid(RaceLogEvent e) {
                return e.getPassId() == getCurrentPassId();
            }
        };
    }
}
