package com.sap.sailing.mongodb.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.Positioned;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.mongodb.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {

    @Override
    public DBObject storeWind(Wind wind) {
        DBObject result = new BasicDBObject();
        storePositioned(wind, result);
        storeTimed(wind, result);
        storeSpeedWithBearing(wind, result);
        return result;
    }

    private void storeTimed(Timed timed, DBObject result) {
        result.put(FieldNames.TIME_AS_MILLIS.name(), timed.getTimePoint().asMillis());
    }

    private void storeSpeedWithBearing(SpeedWithBearing speedWithBearing, DBObject result) {
        storeSpeed(speedWithBearing, result);
        storeBearing(speedWithBearing.getBearing(), result);
        
    }

    private void storeBearing(Bearing bearing, DBObject result) {
        result.put(FieldNames.DEGREE_BEARING.name(), bearing.getDegrees());
    }

    private void storeSpeed(Speed speed, DBObject result) {
        result.put(FieldNames.KNOT_SPEED.name(), speed.getKnots());
    }

    private void storePositioned(Positioned positioned, DBObject result) {
        result.put(FieldNames.LAT_DEG.name(), positioned.getPosition().getLatDeg());
        result.put(FieldNames.LNG_DEG.name(), positioned.getPosition().getLngDeg());
    }

    @Override
    public void addWindTrackDumper(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource, DB database) {
        WindTrack windTrack = trackedRace.getWindTrack(windSource);
        windTrack.addListener(new MongoWindListener(trackedEvent, trackedRace, windSource, this, database));
    }

    @Override
    public DBCollection getWindTrackCollection(DB database) {
        // TODO Auto-generated method stub
        return null;
    }

}
