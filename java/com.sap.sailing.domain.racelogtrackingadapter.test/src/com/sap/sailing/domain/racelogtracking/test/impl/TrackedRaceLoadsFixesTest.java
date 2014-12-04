package com.sap.sailing.domain.racelogtracking.test.impl;

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.test.AbstractGPSFixStoreTest;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRegattaImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;

public class TrackedRaceLoadsFixesTest extends AbstractGPSFixStoreTest {
    private final BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");
    
    private int numberOfSwitchesBetweenLoadingRaceLogs = 0;
    private Integer previouslyLoading = null;
    private int numFixesReceived = 0;
        
    @Test
    public void areFixesStoredInDb() throws TransformationException, NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null);
        Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
        DeviceIdentifier device2 = new SmartphoneImeiIdentifier("imei2");
        DeviceIdentifier device3 = new SmartphoneImeiIdentifier("imei3");
        Course course = new CourseImpl("course", Arrays.asList(new Waypoint[] {
                new WaypointImpl(mark), new WaypointImpl(mark2)}));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] {comp, comp2}));

        map(comp, device, 0, 20000);
        map(comp2, device2, 0, 600);
        //reuse device for two marks
        map(mark, device3, 0, 600);
        map(mark2, device3, 0, 600);

        store.storeFix(device, createFix(100, 10, 20, 30, 40));
        store.storeFix(device, createFix(200, 10, 20, 30, 40));
        store.storeFix(device2, createFix(100, 10, 20, 30, 40));
        store.storeFix(device3, createFix(100, 10, 20, 30, 40));
        store.storeFix(device3, createFix(100, 10, 20, 30, 40));
        
        for (int i=0; i<10000; i++) {
            store.storeFix(device, createFix(i+1000, 10, 20, 30, 40));
        }

        TrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()),
                boatClass, /*startDate*/ null, /*endDate*/ null, null, null, "a", null));
        DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race, Collections.<Sideline>emptyList(),
                EmptyWindStore.INSTANCE, store, 0, 0, 0);
        
        trackedRace.attachRaceLog(raceLog);
        trackedRace.waitForLoadingFromGPSFixStoreToFinishRunning(raceLog);

        testLength(trackedRace.getTrack(comp), 10002);
        testLength(trackedRace.getTrack(comp2), 1);
        testLength(trackedRace.getOrCreateTrack(mark), 1);
        testLength(trackedRace.getOrCreateTrack(mark2), 1);
    }
    
    @Test
    public void metadataStoredInDb() throws TransformationException, NoCorrespondingServiceRegisteredException {    
        assertEquals(0, store.getNumberOfFixes(device));
        assertEquals(null, store.getTimeRangeCoveredByFixes(device));

        map(comp, device, 0, 600);

        store.storeFix(device, createFix(100, 10, 20, 30, 40));
        store.storeFix(device, createFix(200, 10, 20, 30, 40));
        
        assertEquals(2, store.getNumberOfFixes(device));
        assertEquals(TimeRangeImpl.create(100, 200), store.getTimeRangeCoveredByFixes(device));
    }
    
    @Test
    public void attachTwoRaceLogsAndSeeIfLoadingIsSerializedNicely()
            throws TransformationException, NoCorrespondingServiceRegisteredException, InterruptedException {
        Course course = new CourseImpl("course", Collections.<Waypoint>emptyList());
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] {comp}));
        RaceLog raceLog2 = new RaceLogImpl("raceLog 2");
        
        final int numFixes = 10000;
        map(comp, device, 0, numFixes/2);
        map(raceLog2, comp, device, numFixes/2+1, numFixes);
        
        for (int i=0; i<numFixes; i++) {
            store.storeFix(device, createFix(i, 10, 20, 30, 40));
        }

        TrackedRegatta regatta = new DynamicTrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, RegattaImpl.getDefaultName("regatta", boatClass.getName()),
                boatClass, /*startDate*/ null, /*endDate*/ null, null, null, "a", null));
        DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race, Collections.<Sideline>emptyList(),
                EmptyWindStore.INSTANCE, store, 0, 0, 0);
        
        trackedRace.addListener(new AbstractRaceChangeListener() {
            @Override
            public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
                numFixesReceived++;
                int currentlyLoading = fix.getTimePoint().asMillis() <= numFixes/2 ? 1 : 2;
                if (previouslyLoading == null) {
                    previouslyLoading = currentlyLoading;
                } else if (previouslyLoading != currentlyLoading) {
                    previouslyLoading = currentlyLoading;
                    numberOfSwitchesBetweenLoadingRaceLogs++;
                }
            }
        });

        trackedRace.attachRaceLog(raceLog);
        trackedRace.attachRaceLog(raceLog2);

        trackedRace.waitForLoadingFromGPSFixStoreToFinishRunning(raceLog);
        trackedRace.waitForLoadingFromGPSFixStoreToFinishRunning(raceLog2);
        
        assertEquals(1, numberOfSwitchesBetweenLoadingRaceLogs);
        assertEquals(numFixes, numFixesReceived);
    }
}
