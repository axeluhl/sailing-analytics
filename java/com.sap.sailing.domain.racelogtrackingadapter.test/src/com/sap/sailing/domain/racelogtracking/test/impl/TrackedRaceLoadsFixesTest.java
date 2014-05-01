package com.sap.sailing.domain.racelogtracking.test.impl;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

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
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelogtracking.test.AbstractGPSFixStoreTest;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.TrackedRegattaImpl;

public class TrackedRaceLoadsFixesTest extends AbstractGPSFixStoreTest {
    @Test
    public void areFixesStoredInDb() throws TransformationException, NoCorrespondingServiceRegisteredException, InterruptedException {
        Competitor comp2 = DomainFactory.INSTANCE.getOrCreateCompetitor("comp2", "comp2", null, null, null);
        Mark mark2 = DomainFactory.INSTANCE.getOrCreateMark("mark2");
        DeviceIdentifier device2 = new SmartphoneImeiIdentifier("imei2");
        DeviceIdentifier device3 = new SmartphoneImeiIdentifier("imei3");
        BoatClass boatClass = DomainFactory.INSTANCE.getOrCreateBoatClass("49er");

        Course course = new CourseImpl("course", Arrays.asList(new Waypoint[] {new WaypointImpl(mark), new WaypointImpl(mark2)}));
        RaceDefinition race = new RaceDefinitionImpl("race", course, boatClass, Arrays.asList(new Competitor[] {comp, comp2}));

        map(comp, device, 0, 600);
        map(comp2, device2, 0, 600);
        //reuse device for two marks
        map(mark, device3, 0, 600);
        map(mark2, device3, 0, 600);

        store.storeFix(device, createFix(100, 10, 20, 30, 40));
        store.storeFix(device, createFix(101, 10, 20, 30, 40));
        store.storeFix(device2, createFix(100, 10, 20, 30, 40));
        store.storeFix(device3, createFix(100, 10, 20, 30, 40));
        store.storeFix(device3, createFix(100, 10, 20, 30, 40));

        TrackedRegatta regatta = new TrackedRegattaImpl(new RegattaImpl(EmptyRaceLogStore.INSTANCE, "regatta", boatClass, null, null, "a", null));
        DynamicTrackedRaceImpl trackedRace = new DynamicTrackedRaceImpl(regatta, race, Collections.<Sideline>emptyList(),
                EmptyWindStore.INSTANCE, store, 0, 0, 0);
        trackedRace.attachRaceLog(raceLog);
        trackedRace.waitUntilLoadingFromStoresComplete();

        testLength(trackedRace.getTrack(comp), 2);
        testLength(trackedRace.getTrack(comp2), 1);
        testLength(trackedRace.getOrCreateTrack(mark), 1);
        testLength(trackedRace.getOrCreateTrack(mark2), 1);
    }
}
