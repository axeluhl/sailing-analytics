package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.RacingProceduresConfigurationJsonSerializer;

public class RacingProceduresConfigurationJsonDeserializer implements JsonDeserializer<RacingProceduresConfiguration> {

    private RRS26ConfigurationJsonDeserializer rrs26Deserializer;
    private GateStartConfigurationJsonDeserializer gateStartDeserializer;
    private ESSConfigurationJsonDeserializer essDeserializer;
    
    public static RacingProceduresConfigurationJsonDeserializer create() {
        return new RacingProceduresConfigurationJsonDeserializer(RRS26ConfigurationJsonDeserializer.create(),
                GateStartConfigurationJsonDeserializer.create(), ESSConfigurationJsonDeserializer.create());
    }
    
    public RacingProceduresConfigurationJsonDeserializer(RRS26ConfigurationJsonDeserializer rrs26, 
            GateStartConfigurationJsonDeserializer gateStart, ESSConfigurationJsonDeserializer ess) {
        this.rrs26Deserializer = rrs26;
        this.gateStartDeserializer = gateStart;
        this.essDeserializer = ess;
    }
    
    @Override
    public RacingProceduresConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        RacingProceduresConfigurationImpl configuration = createConfiguration();
        
        if (object.containsKey(RacingProceduresConfigurationJsonSerializer.FIELD_RRS26)) {
            RRS26Configuration rrs26Configuration = rrs26Deserializer.deserialize(
                    Helpers.getNestedObjectSafe(object, RacingProceduresConfigurationJsonSerializer.FIELD_RRS26));
            configuration.setRRS26Configuration(rrs26Configuration);
        }
        
        if (object.containsKey(RacingProceduresConfigurationJsonSerializer.FIELD_GATE_START)) {
            GateStartConfiguration gateStartConfiguration = 
                    gateStartDeserializer.deserialize(
                            Helpers.getNestedObjectSafe(object, RacingProceduresConfigurationJsonSerializer.FIELD_GATE_START));
            configuration.setGateStartConfiguration(gateStartConfiguration);
        }
        
        if (object.containsKey(RacingProceduresConfigurationJsonSerializer.FIELD_ESS)) {
            ESSConfiguration essConfiguration = essDeserializer.deserialize(
                    Helpers.getNestedObjectSafe(object, RacingProceduresConfigurationJsonSerializer.FIELD_ESS));
            configuration.setESSConfiguration(essConfiguration);
        }
        
        return configuration;
    }
    
    protected RacingProceduresConfigurationImpl createConfiguration() {
        return new RacingProceduresConfigurationImpl();
    }

}
