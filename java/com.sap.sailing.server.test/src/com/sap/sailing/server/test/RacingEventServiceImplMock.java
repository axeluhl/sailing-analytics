package com.sap.sailing.server.test;

import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.impl.DataImportProgressImpl;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.masterdata.DataImportLockWithProgress;
import com.sap.sse.common.TypeBasedServiceFinderFactory;

public class RacingEventServiceImplMock extends RacingEventServiceImpl {

    private DataImportLockWithProgress lock;

    public RacingEventServiceImplMock() {
        super(false, null);
    }
    
    public RacingEventServiceImplMock(DataImportProgressImpl dataImportProgressImpl, TypeBasedServiceFinderFactory serviceFinderFactory) {
        super(null, null, serviceFinderFactory);
        lock = new DataImportLockWithProgress();
        lock.addProgress(dataImportProgressImpl.getOperationId(), dataImportProgressImpl);
    }

    public RacingEventServiceImplMock(DataImportProgressImpl dataImportProgressImpl) {
        lock = new DataImportLockWithProgress();
        lock.addProgress(dataImportProgressImpl.getOperationId(), dataImportProgressImpl);
    }

    public Map<Regatta, Set<RaceTracker>> getRaceTrackersByRegattaMap() {
        return raceTrackersByRegatta;
    }

    public Map<Object, RaceTracker> getRaceTrackersByIDMap() {
        return raceTrackersByID;
    }

    public Map<String, Regatta> getRegattasByName() {
        return regattasByName;
    }

    @Override
    public DataImportLockWithProgress getDataImportLock() {
        return lock;
    }
}
