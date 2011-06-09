package com.sap.sailing.mongodb.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tracking.impl.WindTrackImpl;
import com.sap.sailing.mongodb.DomainObjectFactory;

public class DomainObjectFactoryImpl implements DomainObjectFactory {

    @Override
    public Wind loadWind(DBObject object) {
        return new WindImpl(loadPosition(object), loadTimePoint(object), loadSpeedWithBearing(object));
    }

    private Position loadPosition(DBObject object) {
        return new DegreePosition((Double) object.get(FieldNames.LAT_DEG.name()), (Double) object.get(FieldNames.LNG_DEG.name()));
    }

    private TimePoint loadTimePoint(DBObject object) {
        return new MillisecondsTimePoint((Long) object.get(FieldNames.TIME_AS_MILLIS.name()));
    }

    private SpeedWithBearing loadSpeedWithBearing(DBObject object) {
        return new KnotSpeedWithBearingImpl((Double) object.get(FieldNames.KNOT_SPEED.name()),
                new DegreeBearingImpl((Double) object.get(FieldNames.DEGREE_BEARING.name())));
    }

    @Override
    public WindTrack loadWindTrack(Event event, RaceDefinition race, WindSource windSource, long millisecondsOverWhichToAverage,
            DB database) {
        WindTrack result = new WindTrackImpl(millisecondsOverWhichToAverage);
        BasicDBObject query = new BasicDBObject();
        query.put(FieldNames.EVENT_NAME.name(), event.getName());
        query.put(FieldNames.RACE_NAME.name(), race.getName());
        query.put(FieldNames.WIND_SOURCE_NAME.name(), windSource.name());
        DBCollection windTracks = database.getCollection(CollectionNames.WIND_TRACKS.name());
        for (DBObject o : windTracks.find(query)) {
            Wind wind = loadWind((DBObject) o.get(FieldNames.WIND.name()));
            result.add(wind);
        }
        return result;
    }

}
