package com.sap.sailing.mongodb.impl;

import com.mongodb.DBObject;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
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

}
