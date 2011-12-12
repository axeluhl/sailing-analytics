package com.sap.sailing.server.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.EventImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.tracking.RaceTracker;

public class RaceTrackerStartStopTest {

    private final static String EVENTNAME = "TESTEVENT";
    private final static String BOATCLASSNAME = "HAPPYBOATCLASS";

    private RacingEventServiceImplMock racingEventService;
    private Event event;
    private BoatClass boatClass;
    private Set<RaceTracker> raceTrackerSet = new HashSet<RaceTracker>();

    private RaceDefinition raceDef1;
    private RaceDefinition raceDef2;
    private RaceDefinition raceDef3;

    private RaceTrackerMock raceTracker1;
    private RaceTrackerMock raceTracker2;
    private RaceTrackerMock raceTracker3;

    @Before
    public void setUp() {
        racingEventService = new RacingEventServiceImplMock();
        boatClass = new BoatClassImpl(BOATCLASSNAME);
        event = new EventImpl(EVENTNAME, boatClass);
        racingEventService.getEventsByNameMap().put(EVENTNAME, event);
        raceTrackerSet = new HashSet<RaceTracker>();
        raceDef1 = new RaceDefinitionImpl("racedef1", null, new BoatClassImpl(BOATCLASSNAME), null);
        raceDef2 = new RaceDefinitionImpl("racedef2", null, new BoatClassImpl(BOATCLASSNAME), null);
        raceDef3 = new RaceDefinitionImpl("racedef3", null, new BoatClassImpl(BOATCLASSNAME), null);
        Set<RaceDefinition> raceDefinitioSetRace1 = new HashSet<RaceDefinition>();
        raceDefinitioSetRace1.add(raceDef1);
        Set<RaceDefinition> raceDefinitioSetRace2 = new HashSet<RaceDefinition>();
        raceDefinitioSetRace2.add(raceDef1);
        raceDefinitioSetRace2.add(raceDef2);
        Set<RaceDefinition> raceDefinitioSetRace3 = new HashSet<RaceDefinition>();
        raceDefinitioSetRace3.add(raceDef1);
        raceDefinitioSetRace3.add(raceDef2);
        raceDefinitioSetRace3.add(raceDef3);

        Long trackerID1 = new Long(1);
        Long trackerID2 = new Long(2);
        Long trackerID3 = new Long(3);
        raceTracker1 = new RaceTrackerMock(new Long(1), event, raceDefinitioSetRace1, true);
        raceTracker2 = new RaceTrackerMock(new Long(2), event, raceDefinitioSetRace2, true);
        raceTracker3 = new RaceTrackerMock(new Long(3), event, raceDefinitioSetRace3, true);
        raceTrackerSet.add(raceTracker1);
        raceTrackerSet.add(raceTracker2);
        raceTrackerSet.add(raceTracker3);
        racingEventService.getRaceTrackersByEventMap().put(event, raceTrackerSet);
        racingEventService.getRaceTrackersByIDMap().put(trackerID1, raceTracker1);
        racingEventService.getRaceTrackersByIDMap().put(trackerID2, raceTracker2);
        racingEventService.getRaceTrackersByIDMap().put(trackerID3, raceTracker3);
    }

    @Test
    public void testStopTrackingRace() throws MalformedURLException, IOException, InterruptedException {
        racingEventService.stopTracking(event, raceDef2);
        assertTrue(racingEventService.getRaceTrackersByIDMap().containsValue(raceTracker1));
        assertFalse(racingEventService.getRaceTrackersByIDMap().containsValue(raceTracker2));
        assertFalse(racingEventService.getRaceTrackersByIDMap().containsValue(raceTracker3));
        // The RaceTrakcersByEvent map should contain a tracker with a set of RaceDefinitions, containing the
        // raceDefinition1
        assert racingEventService.getRaceTrackersByEventMap().size() == 1;
        Iterator<RaceTracker> raceTrackerIter = racingEventService.getRaceTrackersByEventMap().get(event).iterator();
        while(raceTrackerIter.hasNext()){
            RaceTracker currentTracker = raceTrackerIter.next();
            assert(currentTracker.equals(raceTracker1));
        }
    }

    @Test
    public void testService() {

    }

}
