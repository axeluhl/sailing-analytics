package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.MongoObjectFactoryImpl;
import com.sap.sailing.domain.persistence.racelog.tracking.FixMongoHandler;
import com.sap.sse.common.TimePoint;

public class GPSFixMongoHandlerImpl implements FixMongoHandler<GPSFix> {
    private final MongoObjectFactoryImpl mof;
    private final DomainObjectFactoryImpl dof;

    public GPSFixMongoHandlerImpl(MongoObjectFactory mof, DomainObjectFactory dof) {
        this.mof = (MongoObjectFactoryImpl) mof;
        this.dof = (DomainObjectFactoryImpl) dof;
    }

    @Override
    public DBObject transformForth(GPSFix fix) throws IllegalArgumentException {
        DBObject result = new BasicDBObject();
        mof.storeTimed(fix, result);
        mof.storePositioned(fix, result);        
        return result;
    }

    @Override
    public GPSFix transformBack(DBObject object) {
        DBObject dbObject = (DBObject) object;
        TimePoint timePoint = dof.loadTimePoint(dbObject);
        Position position = dof.loadPosition(dbObject);
        return new GPSFixImpl(position, timePoint);
    }

}
