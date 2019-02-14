package com.sap.sailing.xmlexport.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets10LastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.test.OnlineTracTracBasedTest;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sailing.xmlexport.LeaderboardData;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class XMLExportTest extends OnlineTracTracBasedTest {
    
    public XMLExportTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    private void setUpESS2014Nice(String raceNumber) {
        URI storedUri;
        try {
            storedUri = new URI("file:///" + new File("resources/" + getFileName() + raceNumber + ".mtb").getCanonicalPath().replace('\\', '/'));
            super.setUp(new URL("file:///" + new File("resources/" + getFileName() + raceNumber + ".txt").getCanonicalPath()),
            /* liveUri */null, /* storedUri */storedUri,
                    new ReceiverType[] { ReceiverType.MARKPASSINGS, ReceiverType.RACECOURSE, ReceiverType.MARKPOSITIONS, ReceiverType.RAWPOSITIONS });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        boolean result = getTrackedRace().recordWind(new WindImpl(/* position */null, getTrackedRace().getStartOfRace(), new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(65))),
                new WindSourceImpl(WindSourceType.WEB));
        assert result==true;
    }
    
    protected String getExpectedEventName() {
        return "ESS Nice 2014";
    }

    protected String getFileName() {
        return "event_20141001_ESSNice-Race_";
    }

    @Test
    public void testExportingESSNice2014() { 
        setUpESS2014Nice("1");
        FlexibleLeaderboard leaderboard = new FlexibleLeaderboardImpl("ESS 2014 Nice", new ThresholdBasedResultDiscardingRuleImpl(new int[] {0}), new HighPointFirstGets10LastBreaksTie(), 
                null);
        leaderboard.addRace(getTrackedRace(), "R1", false);
        LeaderboardData leaderboardData = new LeaderboardData(leaderboard);
        try {
            leaderboardData.perform();
        } catch (Exception e) {
            e.printStackTrace();
            fail("No exception should occur");
        }
        String resultData = leaderboardData.getResultXML();
        assertNotNull(resultData);
        int resultDataLength = resultData.length();
        assertTrue("resultData length was " + resultDataLength + ", but expected to be > 220000", resultDataLength > 220000);
    }

}
