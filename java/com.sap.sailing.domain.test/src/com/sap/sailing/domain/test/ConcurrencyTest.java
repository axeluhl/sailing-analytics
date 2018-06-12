package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DouglasPeucker;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.ThreadPoolUtil;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class ConcurrencyTest extends OnlineTracTracBasedTest {
    private static final Logger logger = Logger.getLogger(ConcurrencyTest.class.getName());
    
    public ConcurrencyTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Before
    public void setUp() throws URISyntaxException, IOException, InterruptedException, ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        // load Race8 of RC44 Cup in Sweden
        super.setUp("event_20110815_RCSwedenCu",
        /* raceId */ "event_20110815_RCSwedenCu-Race8",
        /* liveUri */ null,
        /* storedUri */ tractracTunnel ? new URI("tcp://"+tractracTunnelHost+":"+TracTracConnectionConstants.PORT_TUNNEL_STORED) : new URI("tcp://" + TracTracConnectionConstants.HOST_NAME + ":" + TracTracConnectionConstants.PORT_STORED),
                new ReceiverType[] { ReceiverType.MARKPASSINGS,
                ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                        new DegreeBearingImpl(10))), new WindSourceImpl(WindSourceType.WEB));
    }

    @Override
    protected String getExpectedEventName() {
        return "RC44 Sweden Cup";
    }

    @Test
    public void compareSingleThreadedApproximationWithMultiThreaded() {
        int numberOfCPUs = Runtime.getRuntime().availableProcessors();
        logger.info("Number of processors: "+numberOfCPUs);
        assertTrue(numberOfCPUs >= 1);
        Competitor competitor = getCompetitorByName("Team Aqua");
        assertNotNull(competitor);
        DynamicGPSFixTrack<Competitor, GPSFixMoving> teamAquaTrack = getTrackedRace().getTrack(competitor);
        MarkPassing firstMarkPassing = getTrackedRace().getMarkPassings(competitor).first();
        MarkPassing lastMarkPassing = getTrackedRace().getMarkPassings(competitor).last();
        DouglasPeucker<Competitor, GPSFixMoving> dp = new DouglasPeucker<Competitor, GPSFixMoving>(teamAquaTrack);
        long start = System.nanoTime();
        List<GPSFixMoving> approximation1 = dp.approximate(new MeterDistance(3), firstMarkPassing.getTimePoint(), lastMarkPassing.getTimePoint());
        long duration = System.nanoTime()-start;
        logger.info("1 thread: "+duration+"ns");
        logger.info("number of approximation points: "+approximation1.size());

        dp = new DouglasPeucker<Competitor, GPSFixMoving>(teamAquaTrack, ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor());
        start = System.nanoTime();
        List<GPSFixMoving> approximation2 = dp.approximate(new MeterDistance(3), firstMarkPassing.getTimePoint(), lastMarkPassing.getTimePoint());
        duration = System.nanoTime()-start;
        logger.info(""+numberOfCPUs+" threads: "+duration+"ns");
        logger.info("number of approximation points: "+approximation2.size());
        assertEquals(approximation1, approximation2);
    }
}
