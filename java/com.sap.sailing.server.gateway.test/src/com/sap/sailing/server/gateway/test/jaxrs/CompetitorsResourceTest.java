package com.sap.sailing.server.gateway.test.jaxrs;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class CompetitorsResourceTest extends AbstractJaxRsApiTest {
    private final String name = "Heiko KRÖGER";
    private final String id = "af855a56-9726-4a9c-a77e-da955bd289be";
    private final String nationality = "GER";
    private final String countryCode = "DE";
    private final String shortName = "HK";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        DynamicTeam team = new TeamImpl(null, Collections.singleton(new PersonImpl(null, new NationalityImpl(nationality), null, null)), null);
        racingEventService.getBaseDomainFactory().getOrCreateCompetitor(id, name, shortName, null, null, null, team, /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null);
    }

    @Test
    public void testGetCompetitorAsJson() throws Exception {
        String jsonString = competitorsResource.getCompetitor(id).getEntity().toString();
        JSONObject json = Helpers.toJSONObjectSafe(JSONValue.parse(jsonString));
        assertTrue(json.get("id").equals(id));
        assertTrue(json.get("name").equals(name));
        assertTrue(json.get("nationality").equals(nationality));
        assertTrue(json.get("countryCode").equals(countryCode));
    }

}
