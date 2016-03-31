package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import java.net.UnknownHostException;

import com.mongodb.MongoException;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoGPSFixStore;
import com.sap.sailing.domain.persistence.racelog.tracking.MongoGPSFixStoreFactory;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;

public class MongoGPSFixStoreFactoryImpl implements MongoGPSFixStoreFactory {	
    @Override
    public MongoGPSFixStore getMongoGPSFixStore(SensorFixStore sensorFixStore) throws UnknownHostException,
            MongoException {
        return new MongoGPSFixStoreImpl(sensorFixStore);
    }
}
