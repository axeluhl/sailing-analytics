package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.tracking.impl.BoatJsonConstants;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.api.BoatsResource;

public class BoatResourceTest extends AbstractJaxRsApiTest {
    private final String id = "af855a56-9726-4a9c-a77e-da955bd289be";
    private final String boatName = "My boat";
    private final String boatClassName = "49er";
    private final String sailId = "GER 1";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        BoatClass boatClass = new BoatClassImpl(boatClassName, true);
        racingEventService.getBaseDomainFactory().getOrCreateBoat(id, boatName, boatClass, sailId, null);
    }

    @Test
    public void testGetBoatAsJson() throws Exception {
        BoatsResource resource = spyResource(new BoatsResource());
        String jsonString = resource.getBoat(id).getEntity().toString();
        
        JSONObject json = Helpers.toJSONObjectSafe(JSONValue.parse(jsonString));
        assertTrue(json.get(BoatJsonConstants.FIELD_ID).equals(id));
        assertTrue(json.get(BoatJsonConstants.FIELD_NAME).equals(boatName));
        assertTrue(json.get(BoatJsonConstants.FIELD_BOAT_CLASS_NAME).equals(boatClassName));
        assertTrue(json.get(BoatJsonConstants.FIELD_SAIL_ID).equals(sailId));
    }

}
