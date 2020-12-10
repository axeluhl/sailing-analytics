package com.sap.sailing.domain.regattalike;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorHandicapInfoEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.BaseRegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.impl.CompetitorTimeOnDistanceAllowancePerNauticalMileFinder;
import com.sap.sailing.domain.abstractlog.regatta.impl.CompetitorTimeOnTimeFactorFinder;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogEventListener;
import com.sap.sailing.domain.base.BaseCompetitorChangeListener;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.util.WeakReferenceWithCleanerCallback;

public abstract class BaseRegattaLikeImpl implements IsRegattaLike {
    private static final long serialVersionUID = -5629172342837950344L;
    private final RegattaLog regattaLog;
    private final RegattaLikeIdentifier identifier;
    private transient Set<RegattaLikeListener> listeners = new HashSet<>();
    
    private class RegattaLogEventForwarder extends RegattaLogEventListener {
        private final RegattaLikeIdentifier identifier;

        private RegattaLogEventForwarder(RegattaLikeIdentifier identifier) {
            this.identifier = identifier;
        }

        @Override
        protected void eventAdded(RegattaLogEvent event) {
            for (RegattaLikeListener listener : listeners) {
                listener.onRegattaLogEvent(identifier, event);
            }
        }
    }

    public BaseRegattaLikeImpl(final RegattaLikeIdentifier identifier, RegattaLogStore store) {
        regattaLog = store.getRegattaLog(identifier, /*ignoreCache*/ true);
        this.identifier = identifier;
        regattaLog.addListener(new RegattaLogEventForwarder(identifier));
    }
    
    @Override
    public RegattaLog getRegattaLog() {
        return regattaLog;
    }

    @Override
    public RegattaLikeIdentifier getRegattaLikeIdentifier() {
        return identifier;
    }

    @Override
    public void addListener(RegattaLikeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(RegattaLikeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean canBoatsOfCompetitorsChangePerRace() {
        return false;
    }

    @Override
    public CompetitorRegistrationType getCompetitorRegistrationType() {
        return CompetitorRegistrationType.CLOSED;
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<>();
        regattaLog.addListener(new RegattaLogEventForwarder(identifier));
    }
    
    /**
     * Registers a competitor change listener that watches a {@link Competitor} for changes for a specific competitor.
     * If such a change is observed, a {@link Runnable} callback can be invoked by subclass implementations. The
     * reference to the {@link Runnable} callback passed to the constructor is implemented as a {@link WeakReference
     * weak reference}, therefore not keeping the callback from getting garbage collected. Conversely, when the callback
     * becomes eligible for garbage collection, and hence the weak reference is cleared and enqueued, this object will
     * remove itself as listener from the {@link Competitor} and hence no garbage will be left behind.
     * <p>
     * 
     * It is therefore a good idea to couple the {@link Runnable} callback's object life cycle to that of the object it
     * will cater to when it receives the updates, for example by introducing a strong reference from the actual
     * receiver of the update to the {@link Runnable} or using the actual update receiver as the callback by having its
     * type implement the {@link Runnable} interface.
     * <p>
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private abstract static class CompetitorHandicapValueChangeListener implements BaseCompetitorChangeListener {
        private static final long serialVersionUID = -7462757662510693195L;

        private final WeakReferenceWithCleanerCallback<Runnable> callback;
        
        public CompetitorHandicapValueChangeListener(Runnable callback, Competitor competitor) {
            competitor.addCompetitorChangeListener(this);
            this.callback = new WeakReferenceWithCleanerCallback<Runnable>(callback, ()->{
                    // unregister this listener from the regatta log when the callback is no longer strongly reachable:
                    competitor.removeCompetitorChangeListener(this);
                });
        }

        protected void tryToNotifyCallback() {
            final Runnable theCallback = callback.get();
            if (theCallback != null) {
                theCallback.run();
            }
        }
    }

    /**
     * Registers a competitor change listener that watches a {@link Competitor} for time-on-time factor changes for a specific
     * competitor. If such a change is observed, a {@link Runnable} callback is invoked. The reference
     * to the {@link Runnable} callback passed to the constructor is implemented as a {@link WeakReference weak reference},
     * therefore not keeping the callback from getting garbage collected. Conversely, when the callback becomes eligible for
     * garbage collection, and hence the weak reference is cleared and enqueued, this object will remove itself as listener
     * from the {@link Competitor} and hence no garbage will be left behind.<p>
     * 
     * It is therefore a good idea to couple the {@link Runnable} callback's object life cycle to that of the object it will
     * cater to when it receives the updates, for example by introducing a strong reference from the actual receiver of the
     * update to the {@link Runnable} or using the actual update receiver as the callback by having its type implement the
     * {@link Runnable} interface.<p>
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private static class CompetitorTimeOnTimeFactorChangeListener extends CompetitorHandicapValueChangeListener {
        private static final long serialVersionUID = -7462757662510693195L;

        public CompetitorTimeOnTimeFactorChangeListener(Runnable callback, Competitor competitor) {
            super(callback, competitor);
        }

        @Override
        public void timeOnTimeFactorChanged(Double oldTimeOnTimeFactor, Double newTimeOnTimeFactor) {
            if (!Util.equalsWithNull(oldTimeOnTimeFactor, newTimeOnTimeFactor)) {
                tryToNotifyCallback();
            }
        }
    }

    /**
     * Registers a competitor change listener that watches a {@link Competitor} for time-on-time factor changes for a specific
     * competitor. If such a change is observed, a {@link Runnable} callback is invoked. The reference
     * to the {@link Runnable} callback passed to the constructor is implemented as a {@link WeakReference weak reference},
     * therefore not keeping the callback from getting garbage collected. Conversely, when the callback becomes eligible for
     * garbage collection, and hence the weak reference is cleared and enqueued, this object will remove itself as listener
     * from the {@link Competitor} and hence no garbage will be left behind.<p>
     * 
     * It is therefore a good idea to couple the {@link Runnable} callback's object life cycle to that of the object it will
     * cater to when it receives the updates, for example by introducing a strong reference from the actual receiver of the
     * update to the {@link Runnable} or using the actual update receiver as the callback by having its type implement the
     * {@link Runnable} interface.<p>
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private static class CompetitorTimeOnDistanceFactorChangeListener extends CompetitorHandicapValueChangeListener {
        private static final long serialVersionUID = -7462757662510693195L;

        public CompetitorTimeOnDistanceFactorChangeListener(Runnable callback, Competitor competitor) {
            super(callback, competitor);
        }

        @Override
        public void timeOnDistanceAllowancePerNauticalMileChanged(Duration oldTimeOnDistanceAllowancePerNauticalMile,
                Duration newTimeOnDistanceAllowancePerNauticalMile) {
            if (!Util.equalsWithNull(oldTimeOnDistanceAllowancePerNauticalMile, newTimeOnDistanceAllowancePerNauticalMile)) {
                tryToNotifyCallback();
            }
        }
    }

    /**
     * Registers a regatta log change listener that watches a {@link RegattaLog} for handicap value changes for a specific
     * competitor. If such events (or their revocation) are observed, a {@link Runnable} callback is invoked. The reference
     * to the {@link Runnable} callback passed to the constructor is implemented as a {@link WeakReference weak reference},
     * therefore not keeping the callback from getting garbage collected. Conversely, when the callback becomes eligible for
     * garbage collection, and hence the weak reference is cleared and enqueued, this object will remove itself as listener
     * from the {@link RegattaLog} and hence no garbage will be left behind.<p>
     * 
     * It is therefore a good idea to couple the {@link Runnable} callback's object life cycle to that of the object it will
     * cater to when it receives the updates, for example by introducing a strong reference from the actual receiver of the
     * update to the {@link Runnable} or using the actual update receiver as the callback by having its type implement the
     * {@link Runnable} interface.<p>
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private static class RegattaLogCompetitorHandicapValueChangeListener extends BaseRegattaLogEventVisitor {
        private static final Logger logger = Logger
                .getLogger(BaseRegattaLikeImpl.RegattaLogCompetitorTimeOnTimeFactorChangeListener.class.getName());
        
        private final WeakReferenceWithCleanerCallback<Runnable> callback;
        protected final Competitor competitorToLookFor;
        private final RegattaLog logToObserve;
        private final Class<? extends RegattaLogSetCompetitorHandicapInfoEvent> regattaLogEventClass;
        
        public RegattaLogCompetitorHandicapValueChangeListener(Runnable callback, RegattaLog logToObserve, Competitor competitorToLookFor,
                Class<? extends RegattaLogSetCompetitorHandicapInfoEvent> regattaLogEventClass) {
            this.logToObserve = logToObserve;
            this.regattaLogEventClass = regattaLogEventClass;
            logToObserve.addListener(this);
            this.callback = new WeakReferenceWithCleanerCallback<Runnable>(callback, ()->{
                    // unregister this listener from the regatta log when the callback is no longer strongly reachable:
                    logToObserve.removeListener(this);
                });
            this.competitorToLookFor = competitorToLookFor;
        }

        @Override
        public void visit(RegattaLogRevokeEvent event) {
            final RegattaLogEvent revokedEvent;
            logToObserve.lockForRead();
            try {
                revokedEvent = logToObserve.getEventById(event.getRevokedEventId());
            } finally {
                logToObserve.unlockAfterRead();
            }
            if (revokedEvent == null) {
                logger.warning("Unable to find revoked event with ID "+event.getRevokedEventId());
            } else if (regattaLogEventClass.isInstance(revokedEvent) &&
                    ((RegattaLogSetCompetitorHandicapInfoEvent) revokedEvent).getCompetitor() == competitorToLookFor) {
                tryToNotifyCallback();
            }
        }

        protected void tryToNotifyCallback() {
            final Runnable theCallback = callback.get();
            if (theCallback != null) {
                theCallback.run();
            }
        }
    }
    
    /**
     * Registers a regatta log change listener that watches a {@link RegattaLog} for time-on-time factor changes for a specific
     * competitor. If such events (or their revocation) is observed, a {@link Runnable} callback is invoked. The reference
     * to the {@link Runnable} callback passed to the constructor is implemented as a {@link WeakReference weak reference},
     * therefore not keeping the callback from getting garbage collected. Conversely, when the callback becomes eligible for
     * garbage collection, and hence the weak reference is cleared and enqueued, this object will remove itself as listener
     * from the {@link RegattaLog} and hence no garbage will be left behind.<p>
     * 
     * It is therefore a good idea to couple the {@link Runnable} callback's object life cycle to that of the object it will
     * cater to when it receives the updates, for example by introducing a strong reference from the actual receiver of the
     * update to the {@link Runnable} or using the actual update receiver as the callback by having its type implement the
     * {@link Runnable} interface.<p>
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private static class RegattaLogCompetitorTimeOnTimeFactorChangeListener extends RegattaLogCompetitorHandicapValueChangeListener {
        public RegattaLogCompetitorTimeOnTimeFactorChangeListener(Runnable callback, RegattaLog logToObserve, Competitor competitorToLookFor) {
            super(callback, logToObserve, competitorToLookFor, RegattaLogSetCompetitorTimeOnTimeFactorEvent.class);
        }

        @Override
        public void visit(RegattaLogSetCompetitorTimeOnTimeFactorEvent event) {
            if (event.getCompetitor() == competitorToLookFor) {
                tryToNotifyCallback();
            }
        }
    }
    
    /**
     * Registers a regatta log change listener that watches a {@link RegattaLog} for time-on-distance allowance changes for a specific
     * competitor. If such events (or their revocation) is observed, a {@link Runnable} callback is invoked. The reference
     * to the {@link Runnable} callback passed to the constructor is implemented as a {@link WeakReference weak reference},
     * therefore not keeping the callback from getting garbage collected. Conversely, when the callback becomes eligible for
     * garbage collection, and hence the weak reference is cleared and enqueued, this object will remove itself as listener
     * from the {@link RegattaLog} and hence no garbage will be left behind.<p>
     * 
     * It is therefore a good idea to couple the {@link Runnable} callback's object life cycle to that of the object it will
     * cater to when it receives the updates, for example by introducing a strong reference from the actual receiver of the
     * update to the {@link Runnable} or using the actual update receiver as the callback by having its type implement the
     * {@link Runnable} interface.<p>
     * 
     * TODO factor such that most of this can also be used for the ToT stuff...
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private static class RegattaLogCompetitorTimeOnDistanceAllowanceChangeListener extends RegattaLogCompetitorHandicapValueChangeListener {
        public RegattaLogCompetitorTimeOnDistanceAllowanceChangeListener(Runnable callback, RegattaLog logToObserve, Competitor competitorToLookFor) {
            super(callback, logToObserve, competitorToLookFor, RegattaLogSetCompetitorTimeOnTimeFactorEvent.class);
        }

        @Override
        public void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent event) {
            if (event.getCompetitor() == competitorToLookFor) {
                tryToNotifyCallback();
            }
        }
    }
    
    @Override
    public Double getTimeOnTimeFactor(Competitor competitor, Optional<Runnable> changeCallback) {
        changeCallback.ifPresent(callback->new RegattaLogCompetitorTimeOnTimeFactorChangeListener(callback, getRegattaLog(), competitor));
        changeCallback.ifPresent(callback->new CompetitorTimeOnTimeFactorChangeListener(callback, competitor));
        final Double timeOnTimeFactorFromRegattaLog = getTimeOnTimeFactorFromRegattaLog(competitor);
        final Double result;
        if (timeOnTimeFactorFromRegattaLog == null) {
            final Double competitorTimeOnTimeFactorDefault = competitor.getTimeOnTimeFactor();
            if (competitorTimeOnTimeFactorDefault == null) {
                result = 1.0;
            } else {
                result = competitorTimeOnTimeFactorDefault;
            }
        } else {
            result = timeOnTimeFactorFromRegattaLog;
        }
        return result;
    }

    @Override
    public Duration getTimeOnDistanceAllowancePerNauticalMile(Competitor competitor, Optional<Runnable> changeCallback) {
        changeCallback.ifPresent(callback->new RegattaLogCompetitorTimeOnDistanceAllowanceChangeListener(callback, getRegattaLog(), competitor));
        changeCallback.ifPresent(callback->new CompetitorTimeOnDistanceFactorChangeListener(callback, competitor));
        final Duration timeOnDistanceAllowancePerNauticalMileFromRegattaLog = getTimeOnDistanceAllowancePerNauticalMileFromRegattaLog(competitor);
        final Duration result;
        if (timeOnDistanceAllowancePerNauticalMileFromRegattaLog == null) {
            final Duration competitorAllowanceDefault = competitor.getTimeOnDistanceAllowancePerNauticalMile();
            if (competitorAllowanceDefault == null) {
                result = Duration.NULL;
            } else {
                result = competitorAllowanceDefault;
            }
        } else {
            result = timeOnDistanceAllowancePerNauticalMileFromRegattaLog;
        }
        return result;
    }

    private Double getTimeOnTimeFactorFromRegattaLog(Competitor competitor) {
        return new CompetitorTimeOnTimeFactorFinder(getRegattaLog(), competitor).analyze();
    }

    private Duration getTimeOnDistanceAllowancePerNauticalMileFromRegattaLog(Competitor competitor) {
        return new CompetitorTimeOnDistanceAllowancePerNauticalMileFinder(getRegattaLog(), competitor).analyze();
    }
}
