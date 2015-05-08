package com.sap.sailing.domain.regattalike;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogEventListener;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sse.common.Duration;

public class BaseRegattaLikeImpl implements IsRegattaLike {
    private static final long serialVersionUID = -5629172342837950344L;
    private final RegattaLog regattaLog;
    private final RegattaLikeIdentifier identifier;
    private transient Set<RegattaLikeListener> listeners = new HashSet<>();
    
    public BaseRegattaLikeImpl(final RegattaLikeIdentifier identifier, RegattaLogStore store) {
        regattaLog = store.getRegattaLog(identifier, /*ignoreCache*/ true);
        this.identifier = identifier;
        
        regattaLog.addListener(new RegattaLogEventListener() {
            @Override
            protected void eventAdded(RegattaLogEvent event) {
                for (RegattaLikeListener listener : listeners) {
                    listener.onRegattaLogEvent(identifier, event);
                }
            }
        });
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
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<>();
    }

    @Override
    public Double getTimeOnTimeFactor(Competitor competitor) {
        final Double timeOnTimeFactorFromRegattaLog = getTimeOnTimeFactorFromRegattaLog(competitor);
        final Double result;
        if (timeOnTimeFactorFromRegattaLog == null) {
            result = competitor.getTimeOnTimeFactor();
        } else {
            result = timeOnTimeFactorFromRegattaLog;
        }
        return result;
    }

    private Double getTimeOnTimeFactorFromRegattaLog(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration getTimeOnDistanceAllowancePerNauticalMile(Competitor competitor) {
        final Duration timeOnDistanceAllowancePerNauticalMileFromRegattaLog = getTimeOnDistanceAllowancePerNauticalMileFromRegattaLog(competitor);
        final Duration result;
        if (timeOnDistanceAllowancePerNauticalMileFromRegattaLog == null) {
            result = competitor.getTimeOnDistanceAllowancePerNauticalMile();
        } else {
            result = timeOnDistanceAllowancePerNauticalMileFromRegattaLog;
        }
        return result;
    }

    private Duration getTimeOnDistanceAllowancePerNauticalMileFromRegattaLog(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }
}
