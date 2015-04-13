package com.sap.sailing.server.gateway.serialization.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.UUID;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sse.common.Util.Pair;

public class FlatSmartphoneUuidAndGPSFixMovingJsonDeserializerTest {
    @Test
    public void deserialize() throws JsonDeserializationException, ParseException {
        String json = "{\n" + 
                "  \"deviceUuid\" : \"af855a56-9726-4a9c-a77e-da955bd289bf\",\n" + 
                "  \"fixes\" : [\n" + 
                "    {\n" + 
                "      \"timestamp\" : 14144160080000,\n" + 
                "      \"latitude\" : 54.325246,\n" + 
                "      \"longitude\" : 10.148556,\n" + 
                "      \"speed\" : 3.61,\n" + 
                "      \"course\" : 258.11,\n" + 
                "    },\n" + 
                "    {\n" + 
                "      \"timestamp\" : 14144168490000,\n" + 
                "      \"latitude\" : 55.12456,\n" + 
                "      \"longitude\" : 8.03456,\n" + 
                "      \"speed\" : 5.1,\n" + 
                "      \"course\" : 14.2,\n" + 
                "    }\n" + 
                "  ]\n" + 
                "}";
        JsonDeserializer<Pair<UUID, List<GPSFixMoving>>> deserializer = 
                new FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer();
        
        Pair<UUID, List<GPSFixMoving>> result = 
                deserializer.deserialize(Helpers.toJSONObjectSafe(JSONValue.parseWithException(json)));
        assertThat("uuid", result.getA(), equalTo(UUID.fromString("af855a56-9726-4a9c-a77e-da955bd289bf")));
        assertThat("number of fixes", result.getB().size(), equalTo(2));
    }
}
