package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class ManeuverAnalysisTestTornado extends KielWeek2011BasedTest{
    
    private SimpleDateFormat dateFormat;
    private static final int TACK_TOLERANCE = 7000;
    private static final int JIBE_TOLERANCE = 7000;
    private static final int PENALTYCIRCLE_TOLERANCE = 9000;

    private List<Maneuver> maneuversInvalid;

    
    public ManeuverAnalysisTestTornado() throws URISyntaxException, IOException, InterruptedException {
        super();
        super.setUp();
        super.setUp("event_20110609_KielerWoch",
        /* raceId */"04687b2a-9e68-11e0-85be-406186cbf87c", new ReceiverType[] { ReceiverType.MARKPASSINGS,
                ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        KielWeek2011BasedTest.fixApproximateMarkPositionsForWindReadOut(getTrackedRace());
        getTrackedRace().setWindSource(WindSource.WEB);
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                        new DegreeBearingImpl(70))), WindSource.WEB);
        dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
    }
    
    @Test
    public void testDouglasPeuckerForGloorCuanillion() throws ParseException, NoWindException{
        Competitor competitor = getCompetitorByName("Gloor+Cuanillon");
        assertNotNull(competitor);
        Date fromDate = dateFormat.parse("06/23/2011-15:28:20");
        Date toDate = dateFormat.parse("06/23/2011-16:38:01");
        assertNotNull(fromDate);
        assertNotNull(toDate);
        List<Maneuver> maneuvers = getTrackedRace().getManeuvers(competitor, new MillisecondsTimePoint(fromDate),
                new MillisecondsTimePoint(toDate));
        maneuversInvalid = new ArrayList<Maneuver>(maneuvers);
    }
}
