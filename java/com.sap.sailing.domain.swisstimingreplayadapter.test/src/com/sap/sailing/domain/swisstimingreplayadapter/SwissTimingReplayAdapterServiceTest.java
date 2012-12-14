package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.InputStream;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SwissTimingReplayAdapterServiceTest {
    
    @Test
    public void testLoadRaceJson() throws Exception {
        String swissTimingUrlText = "/2012_OSG.json";
        InputStream inputStream = getClass().getResourceAsStream(swissTimingUrlText);
        List<SwissTimingReplayRace> races = SwissTimingReplayService.parseJSONObject(inputStream , swissTimingUrlText);
        assertEquals(201, races.size());
        
        assertEquals("446483", races.get(0).race_id);
        
        SwissTimingReplayRace race_42 = races.get(42);
        assertEquals("Elliott", race_42.boat_class);
        assertEquals("19", race_42.flight_number);
        assertEquals(swissTimingUrlText, race_42.jsonurl);
        assertEquals("live.ota.st-sportservice.com/Data/Log?_rsc=SAW010955&_date=02.08.2012", race_42.link);
        assertEquals("Match 55", race_42.name);
        assertEquals("6264", race_42.race_id);
        assertEquals("SAW010955", race_42.rsc);
        assertEquals("02.08.2012 15:30", SwissTimingReplayService.getStartTimeFormat().format(races.get(42).startTime));
        
        assertEquals("live.ota.st-sportservice.com/Data/Log?_rsc=SAM009901&_date=30.07.2012", races.get(races.size() - 1).link);
    }    
    
    @Test
    public void testRaceData_SAW005906_20120805() throws Exception {
        SwissTimingReplayTestListener replayCountListener = new SwissTimingReplayTestListener();
        SwissTimingReplayParser.readData(getClass().getResourceAsStream("/SAW005906.20120805.replay"), replayCountListener);
        
        assertEquals(0, replayCountListener.keyFrameIndexSum);          
        assertEquals(790, replayCountListener.keyFrameIndexPositionCount);  
        assertEquals(2013, replayCountListener.eotCount);                    
        assertEquals(2012, replayCountListener.frameCount);                  
        assertEquals(2012, replayCountListener.referenceTimestampCount);     
        assertEquals(2012, replayCountListener.referenceLocationCount);      
        assertEquals(2012, replayCountListener.rsc_cidCount);                
        assertEquals(72432, replayCountListener.competitorsCountSum);       
        assertEquals(72432, replayCountListener.competitorsCount);            
        assertEquals(20120, replayCountListener.markCount);                   
        assertEquals(72432, replayCountListener.trackersCountSum);          
        assertEquals(72432, replayCountListener.trackersCount);               
        assertEquals(40240, replayCountListener.rankingsCountSum);          
        assertEquals(40240, replayCountListener.rankingsCount);               
        assertEquals(321920, replayCountListener.rankingMarkCount);                
    }

    @Test
    public void testRaceData_SAW010955_20120802() throws Exception {
        SwissTimingReplayTestListener replayCountListener = new SwissTimingReplayTestListener();
        SwissTimingReplayParser.readData(getClass().getResourceAsStream("/SAW010955.20120802.replay"), replayCountListener);
        
        assertEquals(0, replayCountListener.keyFrameIndexSum);          
        assertEquals(402, replayCountListener.keyFrameIndexPositionCount);  
        assertEquals(404, replayCountListener.eotCount);                    
        assertEquals(403, replayCountListener.frameCount);                  
        assertEquals(403, replayCountListener.referenceTimestampCount);     
        assertEquals(403, replayCountListener.referenceLocationCount);      
        assertEquals(403, replayCountListener.rsc_cidCount);                
        assertEquals(4836, replayCountListener.competitorsCountSum);       
        assertEquals(4836, replayCountListener.competitorsCount);            
        assertEquals(2015, replayCountListener.markCount);                   
        assertEquals(4836, replayCountListener.trackersCountSum);          
        assertEquals(4836, replayCountListener.trackersCount);               
        assertEquals(806, replayCountListener.rankingsCountSum);          
        assertEquals(806, replayCountListener.rankingsCount);               
        assertEquals(2418, replayCountListener.rankingMarkCount);                
    }

    @Test
    @Ignore
    public void printRaceData_SAW005906_20120805() throws Exception {
        SwissTimingReplayParser.readData(getClass().getResourceAsStream("/SAW005906.20120805.replay"), new SwissTimingReplayPrintListener());
    }

    @Test
    @Ignore
    public void printReadRaceData_SAW010955_20120802() throws Exception {
        SwissTimingReplayParser.readData(getClass().getResourceAsStream("/SAW010955.20120802.replay"), new SwissTimingReplayPrintListener());
    }

    @Test
    @Ignore
    public void printReadRaceData_SAM002901() throws Exception {
        SwissTimingReplayParser.readData(getClass().getResourceAsStream("/SAM002901.replay"), new SwissTimingReplayPrintListener());
    }

    @Test
    @Ignore
    public void printReadRaceData_SAM009904_20120731() throws Exception {
        SwissTimingReplayParser.readData(getClass().getResourceAsStream("/SAM009904.20120731.replay"), new SwissTimingReplayPrintListener());
    }

}
