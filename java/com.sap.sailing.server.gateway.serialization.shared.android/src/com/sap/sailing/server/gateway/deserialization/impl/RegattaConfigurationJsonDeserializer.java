package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaConfigurationJsonSerializer;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class RegattaConfigurationJsonDeserializer implements JsonDeserializer<RegattaConfiguration> {

    public static RegattaConfigurationJsonDeserializer create() {
        return new RegattaConfigurationJsonDeserializer(RRS26ConfigurationJsonDeserializer.create(), SWCStartConfigurationJsonDeserializer.create(),
                GateStartConfigurationJsonDeserializer.create(), ESSConfigurationJsonDeserializer.create(),
                RacingProcedureConfigurationJsonDeserializer.create(), LeagueConfigurationJsonDeserializer.create());
    }

    private final RRS26ConfigurationJsonDeserializer rrs26Deserializer;
    private final SWCStartConfigurationJsonDeserializer swcStartDeserializer;
    private final GateStartConfigurationJsonDeserializer gateStartDeserializer;
    private final ESSConfigurationJsonDeserializer essDeserializer;
    private final RacingProcedureConfigurationJsonDeserializer basicDeserializer;
    private final LeagueConfigurationJsonDeserializer leagueDeserializer;

    public RegattaConfigurationJsonDeserializer(RRS26ConfigurationJsonDeserializer rrs26, SWCStartConfigurationJsonDeserializer swcStart,
            GateStartConfigurationJsonDeserializer gateStart, ESSConfigurationJsonDeserializer ess,
            RacingProcedureConfigurationJsonDeserializer basicDeserializer, LeagueConfigurationJsonDeserializer leagueDeserializer) {
        this.rrs26Deserializer = rrs26;
        this.swcStartDeserializer = swcStart;
        this.gateStartDeserializer = gateStart;
        this.essDeserializer = ess;
        this.basicDeserializer = basicDeserializer;
        this.leagueDeserializer = leagueDeserializer;
    }

    @Override
    public RegattaConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        RegattaConfigurationImpl configuration = createConfiguration();

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_DEFAULT_RACING_PROCEDURE_TYPE)) {
            RacingProcedureType type = RacingProcedureType.valueOf(object.get(
                    RegattaConfigurationJsonSerializer.FIELD_DEFAULT_RACING_PROCEDURE_TYPE).toString());
            configuration.setDefaultRacingProcedureType(type);
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_DEFAULT_COURSE_DESIGNER_MODE)) {
            CourseDesignerMode mode = CourseDesignerMode.valueOf(object.get(
                    RegattaConfigurationJsonSerializer.FIELD_DEFAULT_COURSE_DESIGNER_MODE).toString());
            configuration.setDefaultCourseDesignerMode(mode);
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_DEFAULT_PROTEST_TIME_DURATION_MILLIS)) {
            final Number defaultProtestTimeDurationInMillis = (Number) object.get(
                    RegattaConfigurationJsonSerializer.FIELD_DEFAULT_PROTEST_TIME_DURATION_MILLIS);
            configuration.setDefaultProtestTimeDuration(defaultProtestTimeDurationInMillis == null ? null :
                new MillisecondsDurationImpl(defaultProtestTimeDurationInMillis.longValue()));
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_RRS26)) {
            RRS26Configuration rrs26Configuration = rrs26Deserializer.deserialize(
                    Helpers.getNestedObjectSafe(object, RegattaConfigurationJsonSerializer.FIELD_RRS26));
            configuration.setRRS26Configuration(rrs26Configuration);
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_SWC_START)) {
            SWCStartConfiguration swcStartConfiguration = swcStartDeserializer.deserialize(
                    Helpers.getNestedObjectSafe(object, RegattaConfigurationJsonSerializer.FIELD_SWC_START));
            configuration.setSWCStartConfiguration(swcStartConfiguration);
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_GATE_START)) {
            GateStartConfiguration gateStartConfiguration =
                    gateStartDeserializer.deserialize(
                            Helpers.getNestedObjectSafe(object, RegattaConfigurationJsonSerializer.FIELD_GATE_START));
            configuration.setGateStartConfiguration(gateStartConfiguration);
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_ESS)) {
            ESSConfiguration essConfiguration = essDeserializer.deserialize(
                    Helpers.getNestedObjectSafe(object, RegattaConfigurationJsonSerializer.FIELD_ESS));
            configuration.setESSConfiguration(essConfiguration);
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_BASIC)) {
            RacingProcedureConfiguration basicConfiguration = basicDeserializer.deserialize(
                    Helpers.getNestedObjectSafe(object, RegattaConfigurationJsonSerializer.FIELD_BASIC));
            configuration.setBasicConfiguration(basicConfiguration);
        }

        if (object.containsKey(RegattaConfigurationJsonSerializer.FIELD_LEAGUE)) {
            LeagueConfiguration leagueConfiguration = leagueDeserializer.deserialize(
                Helpers.getNestedObjectSafe(object, RegattaConfigurationJsonSerializer.FIELD_LEAGUE));
            configuration.setLeagueConfiguration(leagueConfiguration);
        }

        return configuration;
    }

    protected RegattaConfigurationImpl createConfiguration() {
        return new RegattaConfigurationImpl();
    }

}
