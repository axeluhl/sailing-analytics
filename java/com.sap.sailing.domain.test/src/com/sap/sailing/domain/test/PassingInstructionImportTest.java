package com.sap.sailing.domain.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class PassingInstructionImportTest extends OnlineTracTracBasedTest {

    public PassingInstructionImportTest() throws URISyntaxException, MalformedURLException {
        super();
        // TODO Auto-generated constructor stub
    }

    @Before
    public void setUp() throws MalformedURLException, IOException, InterruptedException, URISyntaxException {
        super.setUp();
        
        super.setUp("event_20131112_ESSFlorian",
        /* raceId */"bca3b490-2dce-0131-27f0-60a44ce903c3", new ReceiverType[] { ReceiverType.RACECOURSE });
        getTrackedRace().recordWind(
                new WindImpl(/* position */null, MillisecondsTimePoint.now(), new KnotSpeedWithBearingImpl(12,
                        new DegreeBearingImpl(10))), new WindSourceImpl(WindSourceType.WEB));
    }

    @Override
    protected String getExpectedEventName() {
        return "ESS Florianopolis 2013";
    }

    @Test
    public void test() {
        for(Waypoint w : getRace().getCourse().getWaypoints()){
            System.out.println(w.getPassingSide());
        }
    }

}
