package com.sap.sailing.domain.persistence.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceBoatSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceCompetitorSensorDataMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMarkMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterBoatEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogSetCompetitorTimeOnTimeFactorEvent;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;

public class MongoRegattaLogStoreVisitor implements RegattaLogEventVisitor {

    private final RegattaLikeIdentifier regattaLikeIdentifier;
    private final MongoObjectFactoryImpl mongoObjectFactory;
    
    final static Logger logger = Logger.getLogger(MongoRegattaLogStoreVisitor.class.getName());

    public MongoRegattaLogStoreVisitor(RegattaLikeIdentifier identifier, MongoObjectFactory mongoObjectFactory) {
        super();
        this.regattaLikeIdentifier = identifier;
        this.mongoObjectFactory = (MongoObjectFactoryImpl) mongoObjectFactory;
    }

    @Override
    public void visit(RegattaLogRevokeEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogDeviceMarkMappingEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogDeviceCompetitorMappingEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogDeviceBoatMappingEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogDeviceCompetitorSensorDataMappingEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }
    
    @Override
    public void visit(RegattaLogDeviceBoatSensorDataMappingEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }
    
    @Override
    public void visit(RegattaLogCloseOpenEndedDeviceMappingEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogRegisterBoatEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogRegisterCompetitorEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnTimeFactorEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogSetCompetitorTimeOnDistanceAllowancePerNauticalMileEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }

    @Override
    public void visit(RegattaLogDefineMarkEvent event) {
        mongoObjectFactory.storeRegattaLogEvent(regattaLikeIdentifier, event);
    }
}
