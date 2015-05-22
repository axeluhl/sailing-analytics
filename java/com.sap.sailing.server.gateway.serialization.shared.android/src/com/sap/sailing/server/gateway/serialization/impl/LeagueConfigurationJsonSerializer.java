package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LeagueConfigurationJsonSerializer extends RacingProcedureConfigurationJsonSerializer implements
        JsonSerializer<RacingProcedureConfiguration> {

    public static LeagueConfigurationJsonSerializer create() {
        return new LeagueConfigurationJsonSerializer();
    }

    public LeagueConfigurationJsonSerializer() {
        super();
    }

    @Override
    public JSONObject serialize(RacingProcedureConfiguration baseObject) {
        JSONObject result = super.serialize(baseObject);
        @SuppressWarnings("unused")
        LeagueConfiguration object = (LeagueConfiguration) baseObject;

        return result;
    }

}