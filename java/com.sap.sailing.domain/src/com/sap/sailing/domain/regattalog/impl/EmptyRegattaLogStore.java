package com.sap.sailing.domain.regattalog.impl;

import java.util.UUID;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.impl.RegattaLogImpl;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.regattalog.RegattaLogStore;

public enum EmptyRegattaLogStore implements RegattaLogStore {
    INSTANCE;

    @Override
    public RegattaLog getRegattaLog(RegattaLikeIdentifier regattaLikeId, boolean ignoreCache) {
        return new RegattaLogImpl(regattaLikeId.getName(), UUID.randomUUID());
    }

    @Override
    public void removeRegattaLog(RegattaLikeIdentifier regattaLikeId) {
    }

    @Override
    public void addImportedRegattaLog(RegattaLog regattaLog, RegattaLikeIdentifier identifier) { 
    }

    @Override
    public void clear() {
    }
}
