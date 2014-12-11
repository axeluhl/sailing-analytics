package com.sap.sailing.server.gateway.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.api.CompetitorsResource;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class CompetitorsJsonExportTest {
    private final String name = "Heiko KRÖGER";
    private final String id = "af855a56-9726-4a9c-a77e-da955bd289be";
    private final String boatClassName = "49er";
    private final String sailID = "GER 1";
    private final String nationality = "GER";
    
    private final @Spy CompetitorsResource competitorsResource = new CompetitorsResource();


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        RacingEventService service = new RacingEventServiceImpl();
        Mockito.doReturn(service).when(competitorsResource).getService();
        
        DynamicTeam team = new TeamImpl(null, Collections.singleton(new PersonImpl(null, new NationalityImpl(nationality), null, null)), null);
        DynamicBoat boat = new BoatImpl(null, new BoatClassImpl(boatClassName, false), sailID);
        service.getBaseDomainFactory().getOrCreateCompetitor(id, name, null, team, boat);
    }

    @Test
    public void testExportRegattasAsJson() throws Exception {
        String jsonString = competitorsResource.getCompetitor(id).getEntity().toString();        
        JSONObject json = Helpers.toJSONObjectSafe(JSONValue.parse(jsonString));
        assertThat("id is correct", json.get("id"), equalTo(id));
        assertThat("name is correct", json.get("name"), equalTo(name));
        assertThat("boatClass is correct", json.get("boatClassName"), equalTo(boatClassName));
        assertThat("sailID is correct", json.get("sailID"), equalTo(sailID));
        assertThat("nationality is correct", json.get("nationality"), equalTo(nationality));
    }

}
