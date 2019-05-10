package com.sap.sailing.domain.test;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class ManeuverLossOnPenaltyCircleTest extends OnlineTracTracBasedTest {

    public ManeuverLossOnPenaltyCircleTest() throws MalformedURLException, URISyntaxException {
    }
    
    protected String getExpectedEventName() {
        return "Sailing Champions League 2015";
    }

    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        URI storedUri = new URI("file:////"+new File("resources/SailingChampionsLeague2015-Race28.mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(new URL("file:////"+new File("resources/SailingChampionsLeague2015-Race28.txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.MARKPOSITIONS, ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS });
        getTrackedRace().recordWind(new WindImpl(/* position */ null, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(13, new DegreeBearingImpl(113))), new WindSourceImpl(WindSourceType.WEB));
    }
    
    @Test
    public void testPenaltyLossForCanottieri() throws NoWindException {
        Competitor canottieri = getCompetitorByName("Club Canottieri Roggero di Lauria");
        final Iterable<Maneuver> maneuversCanottieri = getTrackedRace().getManeuvers(canottieri, getTrackedRace().getStartOfRace(), getTrackedRace().getEndOfRace(), /* waitForLatest */ true);
        final Optional<Maneuver> penaltyCircleCanottieri = StreamSupport.stream(maneuversCanottieri.spliterator(), /* parallel */ false).filter(m->m.getType()==ManeuverType.PENALTY_CIRCLE).findAny();
        assertThat(penaltyCircleCanottieri.get().getManeuverLoss().getProjectedDistanceLost(), greaterThan(new MeterDistance(30)));
    }
}
