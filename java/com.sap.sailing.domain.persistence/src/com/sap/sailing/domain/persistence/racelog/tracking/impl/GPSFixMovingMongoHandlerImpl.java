package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sse.common.TimePoint;

public class GPSFixMovingMongoHandlerImpl implements FixMongoHandler<GPSFixMoving> {
    private final MongoObjectFactoryImpl mof;
    private final DomainObjectFactoryImpl dof;

    public GPSFixMovingMongoHandlerImpl(MongoObjectFactory mof, DomainObjectFactory dof) {
        this.mof = (MongoObjectFactoryImpl) mof;
        this.dof = (DomainObjectFactoryImpl) dof;
    }

    @Override
    public DBObject transformForth(GPSFixMoving fix) throws IllegalArgumentException {
        DBObject result = new BasicDBObject();
        mof.storeTimed(fix, result);
        mof.storePositioned(fix, result);    
        mof.storeSpeedWithBearing(fix.getSpeed(), result);  
        return result;
    }

    @Override
    public GPSFixMoving transformBack(DBObject dbObject) {
        TimePoint timePoint = dof.loadTimePoint(dbObject);
        Position position = dof.loadPosition(dbObject);
        SpeedWithBearing speed = dof.loadSpeedWithBearing(dbObject);
        return new GPSFixMovingImpl(position, timePoint, speed);
    }

}
