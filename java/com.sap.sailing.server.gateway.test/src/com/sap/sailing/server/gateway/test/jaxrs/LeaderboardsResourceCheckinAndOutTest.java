package com.sap.sailing.server.gateway.test.jaxrs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.shared.analyzing.CompetitorsInLogAnalyzer;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.racelogtracking.SmartphoneUUIDIdentifier;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.domain.regattalike.CompetitorRegistrationType;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardsResourceCheckinAndOutTest extends AbstractJaxRsApiTest {
    private CompetitorWithBoat competitor;
    private RegattaLog log;
    private RegattaLeaderboard leaderboard;
    private BoatClass boatClass = new BoatClassImpl("49er", false);
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Competitor c = createCompetitors(1).get(0);
        Boat boat = racingEventService.getBaseDomainFactory().getOrCreateBoat("boat", "boat", boatClass, "GER1", null);
        competitor = racingEventService.getBaseDomainFactory().getOrCreateCompetitorWithBoat(c.getId(), c.getName(), c.getShortName(),
                c.getColor(), c.getEmail(), c.getFlagImage(), (DynamicTeam) c.getTeam(),
                /* timeOnTimeFactor */ null, /* timeOnDistanceAllowancePerNauticalMile */ null, null, (DynamicBoat) boat);
        Regatta regatta = new RegattaImpl("regatta", boatClass, /* canBoatsOfCompetitorsChangePerRace */ false, CompetitorRegistrationType.CLOSED,
                MillisecondsTimePoint.now(), MillisecondsTimePoint.now(), Collections.singleton(new SeriesImpl("series", false, /* isFleetsCanRunInParallel */ true, Collections
                        .singleton(new FleetImpl("fleet")), Arrays.asList("column"), racingEventService)), false,
                new HighPoint(), 0, null, OneDesignRankingMetric::new);
        racingEventService.addRegattaWithoutReplication(regatta);
        leaderboard = racingEventService.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "regatta", new int[] {});
        regatta.registerCompetitor(competitor);
        log = leaderboard.getRegattaLike().getRegattaLog();
    }

    @Test
    public void testCheckinAndCheckout() throws Exception {
        LeaderboardsResource resource = spyResource(new LeaderboardsResource());
        UUID deviceUuid = UUID.randomUUID();

        // checkin
        long fromMillis = 500;
        JSONObject json = new JSONObject();
        json.put(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING, competitor.getId().toString());
        json.put(DeviceMappingConstants.JSON_DEVICE_UUID, deviceUuid.toString());
        json.put(DeviceMappingConstants.JSON_FROM_MILLIS, fromMillis);

        Response response = resource.postCheckin(json.toString(), leaderboard.getName());
        assertThat("checkin returns OK", response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));

        Iterable<Competitor> registeredCompetitors = new CompetitorsInLogAnalyzer<>(log).analyze();
        Map<Competitor, List<DeviceMappingWithRegattaLogEvent<Competitor>>> mappings = new RegattaLogDeviceCompetitorMappingFinder(
                log).analyze();

        assertThat("competitor was registered", Util.size(registeredCompetitors), equalTo(1));
        assertThat("device mappings for competitor exist", mappings.size(), equalTo(1));
        List<DeviceMappingWithRegattaLogEvent<Competitor>> mappingsForC = mappings.get(competitor);
        assertThat("exactly one device mapping for competitor exists", mappingsForC.size(), equalTo(1));
        DeviceMapping<Competitor> mappingForC = mappingsForC.get(0);
        assertThat("that mapping is for the correct device",
                ((SmartphoneUUIDIdentifier) mappingForC.getDevice()).getUUID(), equalTo(deviceUuid));
        assertThat("that mapping starts at the correct timepoint", mappingForC.getTimeRange().from().asMillis(),
                equalTo(fromMillis));
        assertThat("that mapping is open-ended", mappingForC.getTimeRange().hasOpenEnd(), equalTo(true));

        // checkout
        long toMillis = 1000;
        json = new JSONObject();
        json.put(DeviceMappingConstants.JSON_TO_MILLIS, toMillis);
        json.put(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING, competitor.getId().toString());
        json.put(DeviceMappingConstants.JSON_DEVICE_UUID, deviceUuid.toString());

        response = resource.postCheckout(json.toString(), leaderboard.getName());
        assertThat("checkout returns OK", response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));

        mappings = new RegattaLogDeviceCompetitorMappingFinder(log).analyze();
        mappingForC = mappings.get(competitor).get(0);
        assertTrue("mapping now ends at checkout timepoint", mappingForC.getTimeRange().includes(new MillisecondsTimePoint(toMillis)));
        assertFalse("mapping now ends at checkout timepoint", mappingForC.getTimeRange().includes(new MillisecondsTimePoint(toMillis+1)));
    }

}
