package com.sap.sailing.manage2sail.resultimport.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.sap.sailing.manage2sail.EventResultDescriptor;
import com.sap.sailing.manage2sail.Manage2SailEventResultsParserImpl;

public class EventResultJsonServiceTest extends AbstractEventResultJsonServiceTest {
    @Test
    public void testParsingEventResultsFromJson() throws IOException {
        Manage2SailEventResultsParserImpl parser = new Manage2SailEventResultsParserImpl();
        EventResultDescriptor eventResult = parser.getEventResult(getInputStream(EVENT_RESULTS_JSON));
        assertNotNull(eventResult);
        assertEquals(14, eventResult.getRegattaResults().size());
    }
}
