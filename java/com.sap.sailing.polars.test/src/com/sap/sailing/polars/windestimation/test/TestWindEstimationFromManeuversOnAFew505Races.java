package com.sap.sailing.polars.windestimation.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.ScalableWind;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.windestimation.ManeuverBasedWindEstimationTrackImpl;
import com.tractrac.model.lib.api.event.CreateModelException;
import com.tractrac.subscription.lib.api.SubscriberInitializationException;

public class TestWindEstimationFromManeuversOnAFew505Races extends OnlineTracTracBasedTest {

    public TestWindEstimationFromManeuversOnAFew505Races() throws URISyntaxException, MalformedURLException {
        super();
    }

    public void setUp(final String fileBaseName) throws MalformedURLException, IOException, InterruptedException, URISyntaxException,
            ParseException, SubscriberInitializationException, CreateModelException {
        super.setUp();
        URI storedUri = new URI("file:///"+new File("../com.sap.sailing.domain.test/resources/"+fileBaseName+".mtb").getCanonicalPath().replace('\\', '/'));
        super.setUp(new URL("file:///"+new File("../com.sap.sailing.domain.test/resources/"+fileBaseName+".txt").getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.RACECOURSE, ReceiverType.RAWPOSITIONS }); // only the tracks; no mark positions, no wind, no mark passings
    }

    @Test
    public void testWindEstimationFromManeuversOn505KW2011Race2() throws MalformedURLException, IOException,
            InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException,
            CreateModelException, NotEnoughDataHasBeenAddedException {
        setUp("event_20110609_KielerWoch-505_Race_2");
        Wind average = getManeuverBasedAverageWind();
        assertEquals(235, average.getFrom().getDegrees(), 5.0); // wind in this race was from 075deg on average
    }

    @Ignore("The test is currently still red because the clustering doesn't work; see bug 1562 comment #8")
    @Test
    public void testWindEstimationFromManeuversOn505KW2011Race3() throws MalformedURLException, IOException,
            InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException,
            CreateModelException, NotEnoughDataHasBeenAddedException {
        setUp("event_20110609_KielerWoch-505_Race_3");
        Wind average = getManeuverBasedAverageWind();
        assertEquals(245, average.getFrom().getDegrees(), 5.0); // wind in this race was from 075deg on average
    }

    @Ignore("The test is currently still red because the clustering doesn't work; see bug 1562 comment #8")
    @Test
    public void testWindEstimationFromManeuversOn505KW2011Race4() throws MalformedURLException, IOException,
            InterruptedException, URISyntaxException, ParseException, SubscriberInitializationException,
            CreateModelException, NotEnoughDataHasBeenAddedException {
        setUp("event_20110609_KielerWoch-505_Race_4");
        Wind average = getManeuverBasedAverageWind();
        assertEquals(275, average.getFrom().getDegrees(), 5.0); // wind in this race was from 075deg on average
    }

    private Wind getManeuverBasedAverageWind() throws NotEnoughDataHasBeenAddedException {
        ManeuverBasedWindEstimationTrackImpl windTrack = new ManeuverBasedWindEstimationTrackImpl(new PolarDataServiceImpl(Executors.newFixedThreadPool(4)),
                getTrackedRace(), /* millisecondsOverWhichToAverage */ 30000, /* waitForLatest */ true);
        ScalableWind windSum = null;
        int count = 0;
        windTrack.lockForRead();
        try {
            for (Wind wind : windTrack.getFixes()) {
                final ScalableWind scalableWind = new ScalableWind(wind, /* useSpeed */ true);
                if (windSum == null) {
                    windSum = scalableWind;
                } else {
                    windSum = windSum.add(scalableWind);
                }
                count++;
            }
        } finally {
            windTrack.unlockAfterRead();
        }
        Wind average = windSum.divide(count);
        return average;
    }
}
