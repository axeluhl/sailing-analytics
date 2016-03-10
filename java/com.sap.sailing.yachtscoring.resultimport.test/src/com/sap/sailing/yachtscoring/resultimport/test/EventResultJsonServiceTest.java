package com.sap.sailing.yachtscoring.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.sap.sailing.yachtscoring.EventResultDescriptor;
import com.sap.sailing.yachtscoring.YachtscoringEventResultsParserImpl;

public class EventResultJsonServiceTest {
    private static final String EVENT_RESULTS_JSON = "eventResults.json";
    
    private static final String RESOURCES = "resources/";

    @Test
    public void testParsingEventResultsFromJson() throws IOException {
        YachtscoringEventResultsParserImpl parser = new YachtscoringEventResultsParserImpl();
            
        EventResultDescriptor eventResult = parser.getEventResult(getInputStream(EVENT_RESULTS_JSON));
        assertNotNull(eventResult);
        
        assertEquals(14, eventResult.getRegattaResults().size());
    }
    
    private InputStream getInputStream(String filename) throws FileNotFoundException, IOException {
        return new FileInputStream(getFile(filename));
    }

    private File getFile(String filename) {
        return new File(RESOURCES + filename);
    }
}
