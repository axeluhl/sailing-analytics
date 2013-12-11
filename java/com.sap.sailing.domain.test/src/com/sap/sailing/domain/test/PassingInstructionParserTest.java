package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class PassingInstructionParserTest extends OnlineTracTracBasedTest {

    public PassingInstructionParserTest() throws URISyntaxException, MalformedURLException {
        super();
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
        int i = 0;
        for(Waypoint w : getRace().getCourse().getWaypoints()){
            if(w.getPassingInstructions()==PassingInstruction.Starboard){
                i++;
            }
        }
        assertEquals(2,i);
    }

}
