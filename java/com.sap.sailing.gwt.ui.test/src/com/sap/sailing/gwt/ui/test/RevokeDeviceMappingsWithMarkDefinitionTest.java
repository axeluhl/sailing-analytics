package com.sap.sailing.gwt.ui.test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.ranking.RankingMetricsFactory;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogEventDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RevokeDeviceMappingsWithMarkDefinitionTest {
    private SailingServiceImplMock sailingService;
    private RacingEventService service;
    private final String columnName = "column";
    private final Fleet fleet = new FleetImpl("fleet");
    
    @Before
    public void prepare() {
        sailingService = new SailingServiceImplMock();
        service = sailingService.getRacingEventService();
    }
    
    @Test
    public void testRevoking() throws DoesNotHaveRegattaLogException {
        service.getMongoObjectFactory().getDatabase().dropDatabase();
        Series series = new SeriesImpl("series", false, /* isFleetsCanRunInParallel */ true, Collections.singletonList(fleet),
                Collections.singletonList(columnName), service);
        Regatta regatta = service.createRegatta(RegattaImpl.getDefaultName("regatta", "Laser"), "Laser", /*startDate*/ null, /*endDate*/ null, 
                UUID.randomUUID(), Collections.<Series>singletonList(series),
                false, new HighPoint(), UUID.randomUUID(), /* useStartTimeInference */ true,
                RankingMetricsFactory.getRankingMetricConstructor(RankingMetrics.ONE_DESIGN));
        RegattaLeaderboard leaderboard = service.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "RegattaLeaderboard", new int[] {});
        
        MarkDTO mark = new MarkDTO("mark", "mark");
        GPSFixDTO fix = new GPSFixDTO(MillisecondsTimePoint.now().asDate(), new DegreePosition(30, 40));
        
        sailingService.addMarkToRegattaLog(leaderboard.getName(), mark);
        sailingService.addMarkFix(leaderboard.getName(), columnName, fleet.getName(), mark.getIdAsString(), fix);
        
        sailingService.revokeMarkDefinitionEventInRegattaLog(leaderboard.getName(), mark);
        
        List<RegattaLogEventDTO> eventLog = sailingService.getRegattaLog(leaderboard.getName()).getEntries();
        
        boolean isRevoked;
        for (RegattaLogEventDTO event : eventLog) {
            if (event.getType().equals(RegattaLogDeviceMarkMappingEventImpl.class.getSimpleName())) {
                isRevoked = false;
                for (RegattaLogEventDTO revoke : eventLog) {
                    if (revoke.getInfo().contains(event.getInfo())) {
                        isRevoked = true;
                        break;
                    }
                }
                assertTrue(isRevoked);
            }
        }
    }
}