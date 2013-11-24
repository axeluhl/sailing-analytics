package com.sap.sailing.server.gateway.deserialization.racegroup.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;
import com.sap.sailing.domain.base.racegroup.impl.RaceGroupImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ColorDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.FleetDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceGroupJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.SeriesWithRowsOfRaceGroupSerializer;

public class RaceGroupDeserializer implements JsonDeserializer<RaceGroup> {

    private final JsonDeserializer<BoatClass> boatClassDeserializer;
    private final JsonDeserializer<SeriesWithRows> seriesDeserializer;
    private final JsonDeserializer<RacingProceduresConfiguration> proceduresDeserializer;

    public static RaceGroupDeserializer create(SharedDomainFactory domainFactory,
            JsonDeserializer<RacingProceduresConfiguration> proceduresDeserializer) {
        return new RaceGroupDeserializer(new BoatClassJsonDeserializer(domainFactory), new SeriesWithRowsDeserializer(
                new RaceRowDeserializer(new FleetDeserializer(new ColorDeserializer()), new RaceCellDeserializer(
                        new RaceLogDeserializer(RaceLogEventDeserializer.create(domainFactory))))),
                proceduresDeserializer);
    }

    public RaceGroupDeserializer(JsonDeserializer<BoatClass> boatClassDeserializer,
            JsonDeserializer<SeriesWithRows> seriesDeserializer,
            JsonDeserializer<RacingProceduresConfiguration> proceduresDeserializer) {
        this.boatClassDeserializer = boatClassDeserializer;
        this.seriesDeserializer = seriesDeserializer;
        this.proceduresDeserializer = proceduresDeserializer;
    }

    public RaceGroup deserialize(JSONObject object) throws JsonDeserializationException {
        String name = object.get(RaceGroupJsonSerializer.FIELD_NAME).toString();
        BoatClass boatClass = null;
        CourseArea courseArea = null;
        RacingProcedureType procedure = RacingProcedureType.UNKNOWN;
        CourseDesignerMode designer = CourseDesignerMode.UNKNOWN;
        RacingProceduresConfiguration proceduresConfiguration = null;

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

        if (object.containsKey(RaceGroupJsonSerializer.FIELD_DEFAULT_RACING_PROCEDURE)) {
            procedure = RacingProcedureType.valueOf(object.get(RaceGroupJsonSerializer.FIELD_DEFAULT_RACING_PROCEDURE)
                    .toString());
        }

        if (object.containsKey(RaceGroupJsonSerializer.FIELD_DEFAULT_COURSE_DESIGNER)) {
            designer = CourseDesignerMode.valueOf(object.get(RaceGroupJsonSerializer.FIELD_DEFAULT_COURSE_DESIGNER)
                    .toString());
        }
        
        if (object.containsKey(RaceGroupJsonSerializer.FIELD_RACING_PROCEDURES_CONFIGURATION)) {
            JSONObject value = Helpers.getNestedObjectSafe(object, RaceGroupJsonSerializer.FIELD_RACING_PROCEDURES_CONFIGURATION);
            proceduresConfiguration = proceduresDeserializer.deserialize(value);
        }
        
        return new RaceGroupImpl(name, boatClass, courseArea, series, procedure, designer, proceduresConfiguration);
    }
}