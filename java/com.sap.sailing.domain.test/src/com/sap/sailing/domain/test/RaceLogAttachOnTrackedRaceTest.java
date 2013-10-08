package com.sap.sailing.domain.test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.test.mock.MockedTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RaceLogAttachOnTrackedRaceTest {
    
    private class MyMockedTrackedRace extends MockedTrackedRace {
        private static final long serialVersionUID = -5450144964435682919L;
        
        private RaceLog raceLog;
        
        @Override
        public void detachRaceLog(Serializable identifier) {
            this.raceLog = null;
        }
        
        @Override
        public void attachRaceLog(RaceLog raceLog) {
            this.raceLog = raceLog;
        }
        
        @Override
        public RaceLog getRaceLog(Serializable identifier) {
            return raceLog;
        }
    }
    
    private FlexibleLeaderboard leaderboard;
    
    @Before
    public void setUp() {
        leaderboard = new FlexibleLeaderboardImpl("", null, new LowPoint(), null);
    }
    
    @Test
    public void testAttachDefaultFleet() {
        RaceColumn column = leaderboard.addRaceColumn("R1", false);
        
        TrackedRace trackedRace = new MyMockedTrackedRace();
        Fleet defaultFleet = Util.get(column.getFleets(), 0);
        
        column.setTrackedRace(defaultFleet, trackedRace);
        
        assertSame(column.getRaceLog(defaultFleet), trackedRace.getRaceLog(column.getRaceLogIdentifier(defaultFleet)));
    }
    
    @Test
    public void testAttachAndDetach() {
        RaceColumn column = leaderboard.addRaceColumn("R1", false);
        
        TrackedRace trackedRace = new MyMockedTrackedRace();
        Fleet fleet = Util.get(column.getFleets(), 0);
        
        column.setTrackedRace(fleet, trackedRace);
        column.setTrackedRace(fleet, null);
        
        assertNull(trackedRace.getRaceLog(column.getRaceLogIdentifier(fleet)));
    }
    
    @Test
    public void testAttachSpecificFleets() {
        Fleet[] fleets = new Fleet[] { new FleetImpl("A"), new FleetImpl("B") };
        Fleet aFleet = fleets[0];
        Fleet bFleet = fleets[1];
        RaceColumn column = leaderboard.addRaceColumn("R1", false, fleets);
        
        TrackedRace trackedRace = new MyMockedTrackedRace();
        
        column.setTrackedRace(aFleet, trackedRace);
        
        assertSame(column.getRaceLog(aFleet), trackedRace.getRaceLog(column.getRaceLogIdentifier(aFleet)));
        assertNotSame(column.getRaceLog(bFleet), trackedRace.getRaceLog(column.getRaceIdentifier(bFleet)));
    }
    
    @Test
    public void testReattach() {
        RaceColumn column = leaderboard.addRaceColumn("R1", false);
        
        Fleet fleet = Util.get(column.getFleets(), 0);
        
        TrackedRace firstRace = new MyMockedTrackedRace();
        TrackedRace secondRace = new MyMockedTrackedRace();
        
        column.setTrackedRace(fleet, firstRace);
        column.setTrackedRace(fleet, secondRace);
        
        assertNull(firstRace.getRaceLog(column.getRaceLogIdentifier(fleet)));
        assertSame(column.getRaceLog(fleet), secondRace.getRaceLog(column.getRaceLogIdentifier(fleet)));
    }

}
