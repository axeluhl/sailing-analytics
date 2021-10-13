package com.sap.sailing.domain.yellowbrickadapter.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRace;
import com.sap.sse.common.TimePoint;

public class YellowBrickAdapterTest {
    private static final String RMSR2019 = "rmsr2019";
    private YellowBrickTrackingAdapterImpl adapter;
    
    @Before
    public void setUp() {
        adapter = new YellowBrickTrackingAdapterImpl(/* base domain factory */ null);
    }
    
    @Test
    public void testSimpleUrlConstruction() {
        final String rmsr2019Url = adapter.getUrlForLatestFix(RMSR2019);
        assertEquals("https://yb.tl/API3/Race/rmsr2019/GetPositions?n=1", rmsr2019Url);
    }
    
    @Test
    public void testGetRace() throws IOException, ParseException, java.text.ParseException {
        final YellowBrickRace race = adapter.getYellowBrickRace(RMSR2019);
        assertEquals(RMSR2019, race.getRaceUrl());
        assertEquals(TimePoint.of(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse("2019-10-26T23:55:01Z")), race.getTimePointOfLastFix());
        assertEquals(113, race.getNumberOfCompetitors());
    }
}
