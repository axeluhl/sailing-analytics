package com.sap.sailing.domain.deckmanadapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Positioned;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.deckmanadapter.impl.FieldType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class Record implements Timed, Positioned {
    private static final long serialVersionUID = -7939775022865795801L;

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    
    private final TimePoint timePoint;
    
    private final Position position; 
    
    private final GPSFixMoving gpsFix;
    
    private final Wind wind;
    
    private Map<FieldType, String> fieldsAsString;
    
    public Record(Map<FieldType, String> fieldsAsString) throws ParseException {
        this.fieldsAsString = fieldsAsString;
        timePoint = new MillisecondsTimePoint(dateFormat.parse(this.fieldsAsString.get(FieldType.date)));
        position = new DegreePosition(Double.valueOf(this.fieldsAsString.get(FieldType.latitude)),
                Double.valueOf(this.fieldsAsString.get(FieldType.longitude)));
        final SpeedWithBearing speed = new KnotSpeedWithBearingImpl(Double.valueOf(this.fieldsAsString.get(FieldType.sog)),
                new DegreeBearingImpl(Double.valueOf(this.fieldsAsString.get(FieldType.cog))));
        wind = new WindImpl(position, timePoint, new KnotSpeedWithBearingImpl(Double.valueOf(this.fieldsAsString
                .get(FieldType.tws)), new DegreeBearingImpl(Double.valueOf(this.fieldsAsString.get(FieldType.twd)))));
        gpsFix = new GPSFixMovingImpl(position, timePoint, speed);
    }
    
    public String getField(FieldType fieldType) {
        return fieldsAsString.get(fieldType);
    }

    public Map<FieldType, String> getFieldsAsString() {
        return fieldsAsString;
    }

    public void setFieldsAsString(Map<FieldType, String> fieldsAsString) {
        this.fieldsAsString = fieldsAsString;
    }

    public TimePoint getTimePoint() {
        return timePoint;
    }

    public GPSFixMoving getGpsFix() {
        return gpsFix;
    }

    public Wind getWind() {
        return wind;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
