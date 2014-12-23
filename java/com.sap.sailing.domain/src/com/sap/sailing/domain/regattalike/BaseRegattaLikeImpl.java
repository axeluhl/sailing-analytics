package com.sap.sailing.domain.regattalike;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogEventListener;
import com.sap.sailing.domain.regattalog.RegattaLogStore;

public class BaseRegattaLikeImpl implements IsRegattaLike {
    private static final long serialVersionUID = -5629172342837950344L;
    private final RegattaLog regattaLog;
    private final RegattaLikeIdentifier identifier;
    private transient Set<RegattaLikeListener> listeners = new HashSet<>();
    
    public BaseRegattaLikeImpl(RegattaLikeIdentifier identifier, RegattaLogStore store) {
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
}
