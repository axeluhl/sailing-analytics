package com.sap.sailing.domain.yellowbrickadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.stream.StreamSupport;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class GetPositionsParserTest {
    private GetPositionsParser parser;
    
    @Before
    public void setUp() {
        parser = new GetPositionsParser();
    }
    
    @Test
    public void testParsingSimpleDocumentWithOneFixPerTeam() throws IOException, ParseException, java.text.ParseException {
        final Reader reader = new InputStreamReader(getClass().getResourceAsStream("/GetPositions.json"));
        final PositionsDocument result = parser.parse(reader);
        assertNotNull(result);
        assertEquals("rmsr2019", result.getRaceUrl());
        assertEquals(113, Util.size(result.getTeams()));
        assertEquals(TimePoint.of(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse("2019-10-26T23:03:00Z")),
                StreamSupport.stream(result.getTeams().spliterator(), /* parallel */ true)
                    .filter(tp->tp.getCompetitorName().equals("JEANNE")).findAny().get().getPositions().iterator().next().getGPSFixMoving().getTimePoint());
    }
}
