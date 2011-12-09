package com.sap.sailing.server.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.tracking.RaceTracker;

public class RaceTrackerStartStopTest {
    
    private final static String EVENTNAME = "TESTEVENT";
    private final static String BOATCLASSNAME = "";
    
    private RacingEventServiceImplMock racingEventService;
    private Long trackerID;
    
    private Event event;
    private BoatClass boatClass;
    
    private RaceTracker raceTracker;
    
    public RaceTrackerStartStopTest() {
    }
    
    @Before
    public void setUp(){
        racingEventService = new RacingEventServiceImplMock();
        boatClass = new BoatClassImpl(BOATCLASSNAME);
        event = new EventImpl(EVENTNAME, boatClass);
        racingEventService.getEventsByNameMap().put(EVENTNAME, event);
        
        trackerID = new Long("1");
        raceTracker = new RaceTrackerMock(trackerID);
        Set<RaceTracker> raceTrackerSet = new HashSet<RaceTracker>();
        racingEventService.getRaceTrackersByEventMap().put(event, raceTrackerSet);
        racingEventService.getRaceTrackersByIDMap().put(trackerID, raceTracker);
    }    
    
    @Test
    public void testService(){
        System.out.println("test");
    }
    
}
