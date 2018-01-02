package com.sap.sailing.domain.windfinderadapter.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sse.common.Util.Pair;

public class WindFinderPollerTest {
    @Test
    public void testPolling() throws Exception {
        WindFinderTrackerFactory factory = new WindFinderTrackerFactory();
        final List<Pair<Wind, WindSource>> wind = new ArrayList<>(); // TODO use blocking queue and read from it with a timeout
        final RaceDefinition mockedRaceDefinition = mock(RaceDefinition.class);
        final DynamicTrackedRegatta mockedTrackedRegatta = mock(DynamicTrackedRegatta.class);
        final DynamicTrackedRace mockedTrackedRace = mock(DynamicTrackedRace.class);
        when(mockedTrackedRegatta.getExistingTrackedRace(mockedRaceDefinition)).thenReturn(mockedTrackedRace);
        when(mockedTrackedRegatta.getTrackedRace(mockedRaceDefinition)).thenReturn(mockedTrackedRace);
        when(mockedTrackedRace.recordWind(any(Wind.class), any(WindSourceWithAdditionalID.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                wind.add(new Pair<>(invocation.getArgumentAt(0, Wind.class), invocation.getArgumentAt(1, WindSourceWithAdditionalID.class)));
                return true;
            }
        });
        when(mockedTrackedRace.recordWind(any(Wind.class), any(WindSourceWithAdditionalID.class), /* applyFilter */ anyBoolean())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                wind.add(new Pair<>(invocation.getArgumentAt(0, Wind.class), invocation.getArgumentAt(1, WindSourceWithAdditionalID.class)));
                return true;
            }
        });
        WindTracker tracker = factory.createWindTracker(mockedTrackedRegatta, mockedRaceDefinition, /* correctByDeclination */ false);
        Thread.sleep(5000); // TODO see above; use blocking queue and timeout
        tracker.stop();
        assertEquals(1, wind.size()); // one latest measurement
    }
}
