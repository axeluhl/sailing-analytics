package com.sap.sailing.mongodb.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.tracking.Positioned;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.mongodb.MongoObjectFactory;

public class MongoObjectFactoryImpl implements MongoObjectFactory {
    private final DB database;
    
    public MongoObjectFactoryImpl(DB database) {
        super();
        this.database = database;
    }

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
        if (positioned.getPosition() != null) {
            result.put(FieldNames.LAT_DEG.name(), positioned.getPosition().getLatDeg());
            result.put(FieldNames.LNG_DEG.name(), positioned.getPosition().getLngDeg());
        }
    }

    @Override
    public void addWindTrackDumper(TrackedEvent trackedEvent, TrackedRace trackedRace, WindSource windSource) {
        WindTrack windTrack = trackedRace.getWindTrack(windSource);
        windTrack.addListener(new MongoWindListener(trackedEvent, trackedRace, windSource, this, database));
    }

    @Override
    public DBCollection getWindTrackCollection() {
        return database.getCollection(CollectionNames.WIND_TRACKS.name());
    }

    @Override
    public DBObject storeWindTrackEntry(Event event, RaceDefinition race, WindSource windSource, Wind wind) {
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.EVENT_NAME.name(), event.getName());
        result.put(FieldNames.RACE_NAME.name(), race.getName());
        result.put(FieldNames.WIND_SOURCE_NAME.name(), windSource.name());
        result.put(FieldNames.WIND.name(), storeWind(wind));
        return result;
    }

    @Override
    public void storeTracTracConfiguration(TracTracConfiguration tracTracConfiguration) {
        DBCollection ttConfigCollection = database.getCollection(CollectionNames.TRACTRAC_CONFIGURATIONS.name());
        BasicDBObject result = new BasicDBObject();
        result.put(FieldNames.TT_CONFIG_NAME.name(), tracTracConfiguration.getName());
        for (DBObject equallyNamedConfig : ttConfigCollection.find(result)) {
            ttConfigCollection.remove(equallyNamedConfig);
        }
        result.put(FieldNames.TT_CONFIG_JSON_URL.name(), tracTracConfiguration.getJSONURL());
        result.put(FieldNames.TT_CONFIG_LIVE_DATA_URI.name(), tracTracConfiguration.getLiveDataURI());
        result.put(FieldNames.TT_CONFIG_STORED_DATA_URI.name(), tracTracConfiguration.getStoredDataURI());
        ttConfigCollection.insert(result);
    }

}
