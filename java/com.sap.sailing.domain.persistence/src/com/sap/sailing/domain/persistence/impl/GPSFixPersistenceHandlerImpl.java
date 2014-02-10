package com.sap.sailing.domain.persistence.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.persistence.devices.GPSFixPersistenceHandler;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;

public class GPSFixPersistenceHandlerImpl implements GPSFixPersistenceHandler {
	MongoObjectFactoryImpl mof;
	DomainObjectFactoryImpl dof;

	public GPSFixPersistenceHandlerImpl(MongoObjectFactoryImpl mof, DomainObjectFactoryImpl dof) {
		this.mof = mof;
		this.dof = dof;
	}

	@Override
	public Object store(GPSFix fix) throws IllegalArgumentException {
		DBObject result = new BasicDBObject();
		mof.storeTimed(fix, result);
		mof.storePositioned(fix, result);        
        return result;
	}

	@Override
	public GPSFix load(Object object) {
		DBObject dbObject = (DBObject) object;
        TimePoint timePoint = dof.loadTimePoint(dbObject);
        Position position = dof.loadPosition(dbObject);
        return new GPSFixImpl(position, timePoint);
	}

}
