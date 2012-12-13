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
    public void testReadRaceData_SAW005906_20120805() throws Exception {
        SwissTimingReplayService.readData(getClass().getResourceAsStream("/SAW005906.20120805.replay"), new SwissTimingReplayTestListener());
    }

    @Test
    public void testReadRaceData_SAW010955_20120802() throws Exception {
        SwissTimingReplayService.readData(getClass().getResourceAsStream("/SAW010955.20120802.replay"), new SwissTimingReplayTestListener());
    }

    @Test
    @Ignore
    public void testReadRaceData_SAM002901() throws Exception {
        SwissTimingReplayService.readData(getClass().getResourceAsStream("/SAM002901.replay"), new SwissTimingReplayTestListener());
    }

    @Test
    public void testReadRaceData_SAM009904_20120731() throws Exception {
        SwissTimingReplayService.readData(getClass().getResourceAsStream("/SAM009904.20120731.replay"), new SwissTimingReplayTestListener());
    }

}
