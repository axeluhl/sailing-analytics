package com.sap.sailing.gwt.ui.test;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.PolarSheetGenerationTriggerResponse;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixMovingTrackImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.server.SailingServiceImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.impl.RacingEventServiceImpl;
import com.sap.sailing.server.replication.ReplicationService;

public class PolarSheetGenerationServiceTest {
    
    private static final String BOAT_CLASS = "Forelle";
    
    @Test
    public void testPolarSheetGenerationService() throws InterruptedException {
         
        SailingService service = new MockSailingServiceForPolarSheetGeneration();
        
        List<RegattaAndRaceIdentifier> idList = new ArrayList<RegattaAndRaceIdentifier>();
        idList.add(new RegattaNameAndRaceName("IrgendeineRegatta", "IrgendeinRennen"));
        
        PolarSheetGenerationTriggerResponse triggerData = service.generatePolarSheetForRaces(idList);
        Assert.assertNotNull(triggerData);
        Assert.assertEquals(BOAT_CLASS, triggerData.getBoatClassName());
        Assert.assertNotNull(triggerData.getId());
        
        boolean complete = false;
        PolarSheetsData results = null;
        double timeOut = 10;
        while (!complete && timeOut > 0) {
            Thread.sleep(200);
            timeOut = timeOut - 0.2;
            results = service.getPolarSheetsGenerationResults(triggerData.getId());
            complete = results.isComplete();
        }
        
        Assert.assertTrue(complete);
        Assert.assertNotNull(results);
        
        Assert.assertEquals(4, results.getDataCount());
        Assert.assertEquals(4.0, results.getAveragedPolarDataByWindSpeed()[1][45]);
        Assert.assertEquals(2.0, results.getAveragedPolarDataByWindSpeed()[1][55]);
        Assert.assertEquals(6.0, results.getAveragedPolarDataByWindSpeed()[1][30]);
        
        
        
        
    }
    
    
    @SuppressWarnings("serial")
    private class MockSailingServiceForPolarSheetGeneration extends SailingServiceImpl {
        
        @Override
        protected RacingEventService getService() {
            RacingEventService service = new MockRacingEventServiceForPolarSheetGeneration();
            return service;
        }
        
        @Override
        protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
                BundleContext context) {
            return null;
        }
        
        @Override
        protected ServiceTracker<ReplicationService, ReplicationService> createAndOpenReplicationServiceTracker(
                BundleContext context) {
            return null;
        }
        
        @Override
        protected ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> createAndOpenScoreCorrectionProviderServiceTracker(
                BundleContext bundleContext) {;
            return null;
        }
        
    }
    
    private class MockRacingEventServiceForPolarSheetGeneration extends RacingEventServiceImpl {
        
        @Override
        public TrackedRace getTrackedRace(RegattaAndRaceIdentifier raceIdentifier) {
            MockTrackedRaceForPolarSheetGeneration trackedRace = new MockTrackedRaceForPolarSheetGeneration();
            return trackedRace;
        }
        
    }
    
    @SuppressWarnings("serial")
    private class MockTrackedRaceForPolarSheetGeneration extends MockedTrackedRace {
        

        @Override
        public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = new MockDynamicGPSFixMovinTrackForPolarSheetGeneration<Competitor>(competitor, 0);
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.0, 1.0), new MillisecondsTimePoint(1), new KnotSpeedWithBearingImpl(3.0, new DegreeBearingImpl(-45.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.001, 1.001), new MillisecondsTimePoint(2), new KnotSpeedWithBearingImpl(5.0, new DegreeBearingImpl(-45.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.002, 1.002), new MillisecondsTimePoint(3), new KnotSpeedWithBearingImpl(2.0, new DegreeBearingImpl(-55.0))));
            track.addGPSFix(new MockGPSFixMovingForPolarSheetGeneration(new DegreePosition(1.003, 1.003), new MillisecondsTimePoint(4), new KnotSpeedWithBearingImpl(6.0, new DegreeBearingImpl(-30.0))));
            
            //Should not be taken into consideration because it happens after the race has ended
            track.addGPSFix(new GPSFixMovingImpl(new DegreePosition(2.0, 3.0), new MillisecondsTimePoint(7), new KnotSpeedWithBearingImpl(5.0, new DegreeBearingImpl(-45.0))));
            return track;
        }
        
        @Override
        public Wind getWind(Position p, TimePoint at) {
            Wind wind = new WindImpl(p, at, new KnotSpeedWithBearingImpl(2.0, new DegreeBearingImpl(180.0)));
            return wind;
        }
        
        @Override
        public RaceDefinition getRace() {
            BoatClass forelle = new BoatClassImpl(BOAT_CLASS, true);
            Competitor competitor = new CompetitorImpl(UUID.randomUUID(), "Hans Frantz", new TeamImpl("SAP", null, null), new BoatImpl("Schnelle Forelle", forelle, "GER000"));
            ArrayList<Competitor> competitors = new ArrayList<Competitor>();
            competitors.add(competitor);
            RaceDefinition race = new RaceDefinitionImpl("Forelle1", new CourseImpl("ForelleCourse", new ArrayList<Waypoint>()), forelle, competitors);
            return race;
        }
        
        @Override
        public TimePoint getStartOfRace() {
            return new MillisecondsTimePoint(1);
        }
        
        @Override
        public TimePoint getEndOfRace() {
            return new MillisecondsTimePoint(5);
        }
    }
    
    @SuppressWarnings("serial")
    private class MockGPSFixMovingForPolarSheetGeneration extends GPSFixMovingImpl {

        public MockGPSFixMovingForPolarSheetGeneration(Position position, TimePoint timePoint, SpeedWithBearing speed) {
            super(position, timePoint, speed);
        }
        
        @Override
        public boolean isValid() {
            return true;
        }
        
    }
    
    @SuppressWarnings("serial")
    private class MockDynamicGPSFixMovinTrackForPolarSheetGeneration<ItemType> extends DynamicGPSFixMovingTrackImpl<ItemType> {

        public MockDynamicGPSFixMovinTrackForPolarSheetGeneration(ItemType trackedItem,
                long millisecondsOverWhichToAverage) {
            super(trackedItem, millisecondsOverWhichToAverage);
        }
        
        @Override
        protected boolean isValid(NavigableSet<GPSFixMoving> rawFixes, GPSFixMoving e) {
            return true;
        }
        
    }


}
