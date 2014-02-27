package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.GPSFixMongoHandler;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class GPSFixMovingMongoHandlerImpl implements GPSFixMongoHandler {
    MongoObjectFactoryImpl mof;
    DomainObjectFactoryImpl dof;

    public GPSFixMovingMongoHandlerImpl(MongoObjectFactoryImpl mof, DomainObjectFactoryImpl dof) {
        this.mof = mof;
        this.dof = dof;
    }

    @Override
    public Object transformForth(GPSFix fix) throws IllegalArgumentException {
        DBObject result = new BasicDBObject();
        mof.storeTimed(fix, result);
        mof.storePositioned(fix, result);    
        mof.storeSpeedWithBearing(((GPSFixMoving) fix).getSpeed(), result);  
        return result;
    }

    @Override
    public GPSFix transformBack(Object object) {
        DBObject dbObject = (DBObject) object;
        TimePoint timePoint = dof.loadTimePoint(dbObject);
        Position position = dof.loadPosition(dbObject);
        SpeedWithBearing speed = dof.loadSpeedWithBearing(dbObject);
        return new GPSFixMovingImpl(position, timePoint, speed);
    }

}
