package com.sap.sailing.server.gateway.test.jaxrs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.shared.analyzing.DeviceMarkMappingFinder;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardsResourcePingMarkTest extends AbstractJaxRsApiTest {
    private Mark mark;
    private RegattaLog log;
    private RegattaLeaderboard leaderboard;

    @Before
    public void setUp() {
        super.setUp();
        
        mark = racingEventService.getBaseDomainFactory().getOrCreateMark("id", "name");
        Regatta regatta = new RegattaImpl("regatta", new BoatClassImpl("49er", false), MillisecondsTimePoint.now(),
                MillisecondsTimePoint.now(), Collections.singleton(new SeriesImpl("series", false, Collections
                        .singleton(new FleetImpl("fleet")), Arrays.asList("column"), racingEventService)), false,
                new HighPoint(), 0, null);
        racingEventService.addRegattaWithoutReplication(regatta);
        leaderboard = racingEventService.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "regatta", new int[] {});

        log = leaderboard.getRegattaLike().getRegattaLog();
    }

    @Test
    public void testCheckinAndCheckout() throws Exception {
        LeaderboardsResource resource = spyResource(new LeaderboardsResource());
        doReturn(RaceLogTrackingAdapterFactory.INSTANCE.getAdapter(racingEventService.getBaseDomainFactory())).when(resource).getRaceLogTrackingAdapter();

        Response response = resource.pingMark(GPSFixesResourceTest.FIXES_JSON, leaderboard.getName(), mark.getId().toString());
        assertThat("response is ok", response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        
        DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(UUID.fromString("af855a56-9726-4a9c-a77e-da955bd289bf"));
        assertThat("all fixes stored", racingEventService.getGPSFixStore().getNumberOfFixes(device), equalTo(2L));
        
        Map<Mark, List<DeviceMapping<Mark>>> mappings = new DeviceMarkMappingFinder<>(log).analyze();
        List<DeviceMapping<Mark>> mappingsForMark = mappings.get(mark);
        assertThat("two mappings were created for the two poings", mappingsForMark.size(), equalTo(2));
    }

}
