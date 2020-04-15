package com.sap.sailing.domain.regattalike;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.CompetitorTimeOnDistanceAllowancePerNauticalMileFinder;
import com.sap.sailing.domain.abstractlog.regatta.impl.CompetitorTimeOnTimeFactorFinder;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogEventListener;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sse.common.Duration;

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

    @Override
    public Double getTimeOnTimeFactor(Competitor competitor) {
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
    public Duration getTimeOnDistanceAllowancePerNauticalMile(Competitor competitor) {
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
