package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.lifecycle.LifecycleState;
import com.sap.sailing.domain.lifecycle.impl.TrackedRaceState;
import com.sap.sailing.domain.test.mock.MockedTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class LifecycleTest extends OnlineTracTracBasedTest {

    public LifecycleTest() throws URISyntaxException, MalformedURLException {
        super();
    }

    @Test
    public void testLifecycleInitialization() throws URISyntaxException, NoWindException, IOException, InterruptedException {
        DynamicTrackedRace race = new MockedTrackedRace();
        assertEquals(TrackedRaceState.INITIAL, race.getLifecycle().getCurrentState());        
    }

    @Test
    public void testLifecycleLoadingState() throws URISyntaxException, NoWindException, IOException, InterruptedException {
        super.setUp("event_20110609_KielerWoch", "357c700a-9d9a-11e0-85be-406186cbf87c", ReceiverType.RACECOURSE, ReceiverType.RACESTARTFINISH, ReceiverType.MARKPASSINGS);
        
        /* this happens because setUp() returns before tracking is finished */
        assertEquals(TrackedRaceState.TRACKING_LIVE_DATA, trackedRace.getLifecycle().getCurrentState());
    }
    
    /* Mimic behaviour of TracTracRaceTrackerImpl */

    private void updateStatusOfTrackedRaces(LifecycleState state) {
        updateStatusOfTrackedRace(race, state);
    }

    private void updateStatusOfTrackedRace(RaceDefinition race, LifecycleState state) {
        DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(race);
        if (trackedRace != null) {
            trackedRace.getLifecycle().performTransitionTo(state);
        }
    }

    @Override
    public void storedDataBegin() {
        LifecycleState state = TrackedRaceState.LOADING_STORED_DATA;
        state.updateProperty(TrackedRaceState.PROPERTY_LOADING_INDICATOR, 0);
        updateStatusOfTrackedRaces(state);
    }

    @Override
    public void storedDataEnd() {
        LifecycleState state = TrackedRaceState.TRACKING_LIVE_DATA;
        state.updateProperty(TrackedRaceState.PROPERTY_LOADING_INDICATOR, 1.0);
        updateStatusOfTrackedRaces(state);
        super.storedDataEnd();
    }

    @Override
    public void storedDataProgress(float progress) {
        LifecycleState state = TrackedRaceState.LOADING_STORED_DATA;
        state.updateProperty(TrackedRaceState.PROPERTY_LOADING_INDICATOR, progress);
        updateStatusOfTrackedRaces(state);
        
        DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(race);
        if (trackedRace != null) {
            assertEquals(TrackedRaceState.LOADING_STORED_DATA, trackedRace.getLifecycle().getCurrentState());
        }
    }
    
    @Override
    public void stopped() {
        DynamicTrackedRace trackedRace = getTrackedRegatta().getExistingTrackedRace(race);
        if (trackedRace != null) {
            assertEquals(TrackedRaceState.TRACKING_LIVE_DATA, trackedRace.getLifecycle().getCurrentState());
        }

        updateStatusOfTrackedRaces(TrackedRaceState.FINISHED);
    }

    @Override
    public void storedDataError(String arg0) {
        System.out.println("Error with stored data for race(s) "+race+": "+arg0);
    }

    @Override
    public void liveDataConnectError(String arg0) {
        System.out.println("Error with live data for race(s) "+race+": "+arg0);
    }

}
