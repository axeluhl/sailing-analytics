package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.orc.FixedSpeedImpliedWind;
import com.sap.sailing.domain.common.orc.ImpliedWindSource;
import com.sap.sailing.domain.common.orc.OtherRaceAsImpliedWindSource;
import com.sap.sailing.domain.common.orc.OwnMaxImpliedWind;
import com.sap.sailing.domain.common.orc.impl.FixedSpeedImpliedWindSourceImpl;
import com.sap.sailing.domain.common.orc.impl.OtherRaceAsImpliedWindSourceImpl;
import com.sap.sailing.domain.common.orc.impl.OwnMaxImpliedWindImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.ImpliedWindSourceSerializer;
import com.sap.sse.common.Speed;
import com.sap.sse.common.Util.Triple;

public class ImpliedWindSourceDeserializer implements JsonDeserializer<ImpliedWindSource> {
    private static final Logger logger = Logger.getLogger(ImpliedWindSourceDeserializer.class.getName());
    
    @Override
    public ImpliedWindSource deserialize(JSONObject object) throws JsonDeserializationException {
        final String type = object.get(ImpliedWindSourceSerializer.ORC_IMPLIED_WIND_SOURCE_TYPE).toString();
        final ImpliedWindSource impliedWindSource;
        if (type.equals(FixedSpeedImpliedWind.class.getSimpleName())) {
            final Number impliedWindSpeedInKnotsAsNumber = (Number) object.get(ImpliedWindSourceSerializer.ORC_FIXED_IMPLIED_WIND_SPEED_IN_KNOTS);
            final Speed impliedWindSpeed = impliedWindSpeedInKnotsAsNumber == null ? null : new KnotSpeedImpl(impliedWindSpeedInKnotsAsNumber.doubleValue());
            impliedWindSource = new FixedSpeedImpliedWindSourceImpl(impliedWindSpeed);
        } else if (type.equals(OwnMaxImpliedWind.class.getSimpleName())) {
            impliedWindSource = new OwnMaxImpliedWindImpl();
        } else if (type.equals(OtherRaceAsImpliedWindSource.class.getSimpleName())) {
            impliedWindSource = new OtherRaceAsImpliedWindSourceImpl(new Triple<>(
                    object.get(ImpliedWindSourceSerializer.ORC_OTHER_RACE_REGATTA_LIKE_NAME).toString(),
                    object.get(ImpliedWindSourceSerializer.ORC_OTHER_RACE_RACE_COLUMN_NAME).toString(),
                    object.get(ImpliedWindSourceSerializer.ORC_OTHER_RACE_FLEET_NAME).toString()));
        } else {
            logger.log(Level.SEVERE, "Unknown implied wind source type "+type+" while de-serializing race log event "+object);
            impliedWindSource = null;
        }
        return impliedWindSource;
    }

}
