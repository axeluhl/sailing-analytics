package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RegattaConfigurationJsonSerializer implements JsonSerializer<RegattaConfiguration> {

    public static RegattaConfigurationJsonSerializer create() {
        return new RegattaConfigurationJsonSerializer(RRS26ConfigurationJsonSerializer.create(), SWCStartConfigurationJsonSerializer.create(),
                GateStartConfigurationJsonSerializer.create(), ESSConfigurationJsonSerializer.create(),
                RacingProcedureConfigurationJsonSerializer.create(), LeagueConfigurationJsonSerializer.create());
    }

    public static final String FIELD_DEFAULT_RACING_PROCEDURE_TYPE = "defaultRacingProcedureType";
    public static final String FIELD_DEFAULT_COURSE_DESIGNER_MODE = "defaultCourseDesignerMode";
    public static final String FIELD_DEFAULT_PROTEST_TIME_DURATION_MILLIS = "defaultProtestTimeDurationMillis";
    public static final String FIELD_RRS26 = "rrs26";
    public static final String FIELD_SWC_START = "swcStart";
    public static final String FIELD_GATE_START = "gateStart";
    public static final String FIELD_ESS = "ess";
    public static final String FIELD_LEAGUE = "league";
    public static final String FIELD_BASIC = "basic";
    
    private final JsonSerializer<RacingProcedureConfiguration> rrs26Serializer;
    private final JsonSerializer<RacingProcedureConfiguration> swcStartSerializer;
    private final JsonSerializer<RacingProcedureConfiguration> gateStartSerializer;
    private final JsonSerializer<RacingProcedureConfiguration> essSerializer;
    private final JsonSerializer<RacingProcedureConfiguration> basicSerializer;
    private final JsonSerializer<RacingProcedureConfiguration> leagueSerializer;

    public RegattaConfigurationJsonSerializer(JsonSerializer<RacingProcedureConfiguration> rrs26, JsonSerializer<RacingProcedureConfiguration> swcStart,
            JsonSerializer<RacingProcedureConfiguration> gateStart, JsonSerializer<RacingProcedureConfiguration> ess,
            JsonSerializer<RacingProcedureConfiguration> basicSerializer, JsonSerializer<RacingProcedureConfiguration> leagueSerializer) {
        this.rrs26Serializer = rrs26;
        this.swcStartSerializer = swcStart;
        this.gateStartSerializer = gateStart;
        this.essSerializer = ess;
        this.basicSerializer = basicSerializer;
        this.leagueSerializer = leagueSerializer;
    }

    @Override
    public JSONObject serialize(RegattaConfiguration object) {
        JSONObject result = new JSONObject();
        if (object.getDefaultRacingProcedureType() != null) {
            result.put(FIELD_DEFAULT_RACING_PROCEDURE_TYPE, object.getDefaultRacingProcedureType().name());
        }

        if (object.getDefaultCourseDesignerMode() != null) {
            result.put(FIELD_DEFAULT_COURSE_DESIGNER_MODE, object.getDefaultCourseDesignerMode().name());
        }

        if (object.getDefaultProtestTimeDuration() != null) {
            result.put(FIELD_DEFAULT_PROTEST_TIME_DURATION_MILLIS, object.getDefaultProtestTimeDuration().asMillis());
        }

        if (object.getRRS26Configuration() != null) {
            result.put(FIELD_RRS26, rrs26Serializer.serialize(object.getRRS26Configuration()));
        }

        if (object.getSWCStartConfiguration() != null) {
            result.put(FIELD_SWC_START, swcStartSerializer.serialize(object.getSWCStartConfiguration()));
        }

        if (object.getGateStartConfiguration() != null) {
            result.put(FIELD_GATE_START, gateStartSerializer.serialize(object.getGateStartConfiguration()));
        }
        
        if (object.getESSConfiguration() != null) {
            result.put(FIELD_ESS, essSerializer.serialize(object.getESSConfiguration()));
        }
        
        if (object.getBasicConfiguration() != null) {
            result.put(FIELD_BASIC, basicSerializer.serialize(object.getBasicConfiguration()));
        }
        if (object.getLeagueConfiguration() != null) {
            result.put(FIELD_LEAGUE, leagueSerializer.serialize(object.getLeagueConfiguration()));
        }

        return result;
    }

}
