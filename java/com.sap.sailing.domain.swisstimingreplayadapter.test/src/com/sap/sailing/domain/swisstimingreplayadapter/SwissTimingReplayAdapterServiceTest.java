package com.sap.sailing.domain.swisstimingreplayadapter;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class SwissTimingReplayAdapterServiceTest {
    
    @Test
    public void testLoadRaceJson() throws Exception {
        String swissTimingUrlText = "/2012_OSG.json";
        InputStream inputStream = getClass().getResourceAsStream(swissTimingUrlText);
        List<SwissTimingReplayRace> races = SwissTimingReplayService.parseJSONObject(inputStream , swissTimingUrlText);
        assertEquals(201, races.size());
        
        assertEquals("446483", races.get(0).race_id);
        
        assertEquals("Elliott", races.get(42).boat_class);
        assertEquals("19", races.get(42).flight_number);
        assertEquals(swissTimingUrlText, races.get(42).jsonurl);
        assertEquals("live.ota.st-sportservice.com/Data/Log?_rsc=SAW010955&_date=02.08.2012", races.get(42).link);
        assertEquals("Match 55", races.get(42).name);
        assertEquals("6264", races.get(42).race_id);
        assertEquals("SAW010955", races.get(42).rsc);
        assertEquals("02.08.2012 15:30", SwissTimingReplayService.SWISSTIMING_DATEFORMAT.format(races.get(42).startTime));
        
        assertEquals("live.ota.st-sportservice.com/Data/Log?_rsc=SAM009901&_date=30.07.2012", races.get(races.size() - 1).link);
    }    
    
    @Test
    public void testReadRaceData() throws Exception {
        SwissTimingReplayService.readData(getClass().getResourceAsStream("/SAW005906.20120805.replay"));
    }

}
