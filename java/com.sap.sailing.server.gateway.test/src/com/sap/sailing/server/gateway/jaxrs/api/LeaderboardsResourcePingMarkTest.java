package com.sap.sailing.server.gateway.jaxrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardsResource;
import com.sap.sailing.server.gateway.test.jaxrs.AbstractJaxRsApiTest;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardsResourcePingMarkTest extends AbstractJaxRsApiTest {
    private Mark mark;
    private RegattaLog log;
    private RegattaLeaderboard leaderboard;
    
    public static final String PING_MARK_JSON =
            "    {\n" + 
            "      \"timestamp\" : 1427142552000,\n" + 
            "      \"latitude\" : 54.325246,\n" + 
            "      \"longitude\" : 10.148556,\n" + 
            "      \"speed\" : 3.61,\n" + 
            "      \"course\" : 258.11,\n" + 
            "    }";

    public static final String PING2_MARK_JSON =
            "    {\n" + 
            "      \"timestamp\" : 1427142562000,\n" + 
            "      \"latitude\" : 54.425246,\n" + 
            "      \"longitude\" : 10.248556,\n" + 
            "      \"speed\" : 3.61,\n" + 
            "      \"course\" : 258.11,\n" + 
            "    }";

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
        LeaderboardsResource resource = spyResource(new LeaderboardsResource() {
            @Override
            RaceLogTrackingAdapter getRaceLogTrackingAdapter() {
                return RaceLogTrackingAdapterFactory.INSTANCE.getAdapter(racingEventService.getBaseDomainFactory());
            }
        });
        {
        Response response = resource.pingMark(PING_MARK_JSON, leaderboard.getName(), mark.getId().toString());
        assertThat("response is ok", response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        Map<Mark, List<DeviceMapping<Mark>>> mappings = new DeviceMarkMappingFinder<>(log).analyze();
        List<DeviceMapping<Mark>> mappingsForMark = mappings.get(mark);
        assertThat("one mapping was created for the one ping", mappingsForMark.size(), equalTo(1));
        assertOneFixPerMapping(mappingsForMark);
        }
        {
        // now produce a second ping; this should produce two fixes for the mark: one repeating the last known
        // position, and another one with the new position.
        Response response = resource.pingMark(PING2_MARK_JSON, leaderboard.getName(), mark.getId().toString());
        assertThat("response is ok", response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        Map<Mark, List<DeviceMapping<Mark>>> mappings = new DeviceMarkMappingFinder<>(log).analyze();
        List<DeviceMapping<Mark>> mappingsForMark = mappings.get(mark);
        assertThat("two additional mappings were created for the second ping", mappingsForMark.size(), equalTo(3));
        assertOneFixPerMapping(mappingsForMark);
        }
    }

    private void assertOneFixPerMapping(List<DeviceMapping<Mark>> mappingsForMark) throws TransformationException,
            NoCorrespondingServiceRegisteredException {
        for (DeviceMapping<Mark> i : mappingsForMark) {
            DeviceIdentifier device = i.getDevice();
            // first ping produces exactly one fix:
            assertThat("all fixes stored", racingEventService.getGPSFixStore().getNumberOfFixes(device), equalTo(1L));
        }
    }

}
