package com.sap.sailing.domain.abstractlog.race.impl;

import java.io.Serializable;
import java.util.NavigableSet;
import java.util.logging.Logger;

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
    private final static Logger logger = Logger.getLogger(RaceLogImpl.class.getName());

    private static final long serialVersionUID = 98032278604708475L;
    private int currentPassId;

    public RaceLogImpl(Serializable identifier) {
        super(identifier, new RaceLogEventComparator());
        this.currentPassId = DefaultPassId;
    }

    public RaceLogImpl(String nameForReadWriteLock, Serializable identifier) {
        super(nameForReadWriteLock, identifier, new RaceLogEventComparator());
        this.currentPassId = DefaultPassId;
    }

    @Override
    public int getCurrentPassId() {
        return currentPassId;
    }

    /**
     * Sets a new active pass id. Ignored if new and current are equal.
     * 
     * @param newPassId
     *            to be set.
     */
    public void setCurrentPassId(int newPassId) {
        if (newPassId != this.currentPassId) {
            logger.finer(String.format("Changing pass id to %d", newPassId));
            this.currentPassId = newPassId;
        }
    }
    
    @Override
    protected void onSuccessfulAdd(RaceLogEvent event, boolean notifyListeners) {
        // FIXME with out-of-order delivery would destroy currentPassId; need to check at least the createdAt time
        // point
        setCurrentPassId(Math.max(event.getPassId(), this.currentPassId));
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
