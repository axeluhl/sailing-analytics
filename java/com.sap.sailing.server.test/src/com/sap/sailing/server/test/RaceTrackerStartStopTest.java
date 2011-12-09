package com.sap.sailing.server.test;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;

public class RaceTrackerStartStopTest {
    
    private final static String EVENTNAME = "TESTEVENT";
    
    private RacingEventServiceImplMock racingEventService;
    
    private Event event;
    private BoatClass boatClass;
    
    @Before
    public void setUp(){
        event = new EventImpl(EVENTNAME, BOATCLASS);
    }
    
    @Test
    public RaceTrackerStartStopTest() {
        racingEventService = new RacingEventServiceImplMock();
        racingEventService.getEventsByName().put("Testevent", )
    }
    
}
