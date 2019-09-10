package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

/**
 * Serializer for {@link com.sap.sailing.domain.abstractlog.orc.RaceLogORCLegDataEvent ORCLegDataEvent}.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class RaceLogORCLegDataEventSerializer extends BaseRaceLogEventSerializer {

    public static final String VALUE_CLASS = RaceLogORCLegDataEvent.class.getSimpleName();
    public static final String ORC_LEG_NR = "legNr";
    public static final String ORC_LEG_TWA = "twaDeg";
    public static final String ORC_LEG_LENGTH = "lengthNauticalMiles";
    public static final String ORC_LEG_TYPE = "type";
    
    public RaceLogORCLegDataEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        super(competitorSerializer);
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        RaceLogORCLegDataEvent legData = (RaceLogORCLegDataEvent) object;
        JSONObject result = super.serialize(legData);
        result.put(ORC_LEG_NR, legData.getLegNr());
        result.put(ORC_LEG_LENGTH, legData.getLength().getNauticalMiles());
        result.put(ORC_LEG_TWA, legData.getTwa().getDegrees());
        result.put(ORC_LEG_TYPE, legData.getType().name());
        return result;
    }

    @Override
    protected String getClassFieldValue() {
        return VALUE_CLASS;
    }

}
