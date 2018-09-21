package com.sap.sailing.gwt.ui.test;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.ranking.RankingMetricsFactory;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.server.RacingEventService;

public class PingMarkViaRegattaLogTest {
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
    public void testPinging() throws DoesNotHaveRegattaLogException {
        service.getMongoObjectFactory().getDatabase().dropDatabase();
        Series series = new SeriesImpl("series", false, /* isFleetsCanRunInParallel */ true, Collections.singletonList(fleet),
                Collections.singletonList(columnName), service);
        Regatta regatta = service.createRegatta(RegattaImpl.getDefaultName("regatta", "Laser"), "Laser", 
                /* canBoatsOfCompetitorsChangePerRace */ true, CompetitorRegistrationType.CLOSED, /*startDate*/ null, /*endDate*/ null, 
                UUID.randomUUID(), Collections.<Series>singletonList(series),
                false, new HighPoint(), UUID.randomUUID(), /*buoyZoneRadiusInHullLengths*/2.0, /* useStartTimeInference */ true,
                /* controlTrackingFromStartAndFinishTimes */ false, RankingMetricsFactory.getRankingMetricConstructor(RankingMetrics.ONE_DESIGN));
        RegattaLeaderboard leaderboard = service.addRegattaLeaderboard(regatta.getRegattaIdentifier(), "RegattaLeaderboard", new int[] {});
        
        MarkDTO mark = new MarkDTO("mark", "mark");
        Position position = new DegreePosition(30, 40);
        
        sailingService.pingMark(leaderboard.getName(), mark, /* time point for fix */ null, position);
    }
}
