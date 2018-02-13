package com.sap.sailing.server.gateway.deserialization.racegroup.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.RaceGroupImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ColorDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.FleetDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.TargetTimeInfoDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceGroupJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.SeriesWithRowsOfRaceGroupSerializer;

public class RaceGroupDeserializer implements JsonDeserializer<RaceGroup> {

    private final JsonDeserializer<BoatClass> boatClassDeserializer;
    private final JsonDeserializer<SeriesWithRows> seriesDeserializer;
    private final JsonDeserializer<RegattaConfiguration> configurationDeserializer;

    public static RaceGroupDeserializer create(SharedDomainFactory domainFactory,
            JsonDeserializer<RegattaConfiguration> proceduresDeserializer) {
        return new RaceGroupDeserializer(new BoatClassJsonDeserializer(domainFactory), new SeriesWithRowsDeserializer(
                new RaceRowDeserializer(new FleetDeserializer(new ColorDeserializer()), new RaceCellDeserializer(
                        new RaceLogDeserializer(RaceLogEventDeserializer.create(domainFactory)),
                        new TargetTimeInfoDeserializer(new WindJsonDeserializer(new PositionJsonDeserializer()))))),
                proceduresDeserializer);
    }

    public RaceGroupDeserializer(JsonDeserializer<BoatClass> boatClassDeserializer,
            JsonDeserializer<SeriesWithRows> seriesDeserializer,
            JsonDeserializer<RegattaConfiguration> configurationDeserializer) {
        this.boatClassDeserializer = boatClassDeserializer;
        this.seriesDeserializer = seriesDeserializer;
        this.configurationDeserializer = configurationDeserializer;
    }

    public RaceGroup deserialize(JSONObject object) throws JsonDeserializationException {
        String name = object.get(RaceGroupJsonSerializer.FIELD_NAME).toString();
        BoatClass boatClass = null;
        String displayName = null;
        Boolean canBoatsOfCompetitorsChangePerRace = false;
        CourseArea courseArea = null;
        RegattaConfiguration configuration = null;
        if (object.containsKey(RaceGroupJsonSerializer.FIELD_COURSE_AREA)) {
            // TODO: deserialize CourseArea ...
            // WHY should I?
        }
        if (object.containsKey(RaceGroupJsonSerializer.FIELD_BOAT_CLASS)) {
            boatClass = boatClassDeserializer.deserialize(Helpers.getNestedObjectSafe(object,
                    RaceGroupJsonSerializer.FIELD_BOAT_CLASS));
        }
        Collection<SeriesWithRows> series = new ArrayList<SeriesWithRows>();
        for (Object seriesObject : Helpers.getNestedArraySafe(object, SeriesWithRowsOfRaceGroupSerializer.FIELD_SERIES)) {
            JSONObject seriesJson = Helpers.toJSONObjectSafe(seriesObject);
            series.add(seriesDeserializer.deserialize(seriesJson));
        }
        if (object.containsKey(RaceGroupJsonSerializer.FIELD_REGATTA_CONFIGURATION)) {
            JSONObject value = Helpers.getNestedObjectSafe(object, RaceGroupJsonSerializer.FIELD_REGATTA_CONFIGURATION);
            configuration = configurationDeserializer.deserialize(value);
        }
        if (object.containsKey(RaceGroupJsonSerializer.FIELD_DISPLAY_NAME)) {
            final Object displayNameJson = object.get(RaceGroupJsonSerializer.FIELD_DISPLAY_NAME);
            displayName = displayNameJson == null ? null : displayNameJson.toString();
        }
        if (object.containsKey(RaceGroupJsonSerializer.FIELD_CAN_BOATS_OF_COMPETITORS_CHANGE_PER_RACE)) {
            final Object canBoatsOfCompetitorsChangePerRaceJson = object.get(RaceGroupJsonSerializer.FIELD_CAN_BOATS_OF_COMPETITORS_CHANGE_PER_RACE);
            canBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRaceJson == null ? null : (Boolean) canBoatsOfCompetitorsChangePerRaceJson; 
        }
        return new RaceGroupImpl(name, displayName, boatClass, canBoatsOfCompetitorsChangePerRace, courseArea, series, configuration);
    }
}