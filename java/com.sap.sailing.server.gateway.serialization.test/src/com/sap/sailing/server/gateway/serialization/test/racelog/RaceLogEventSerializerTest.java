package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;

public class RaceLogEventSerializerTest {

    private RaceLogEventSerializer serializer;

    private JsonSerializer<RaceLogEvent> flagEventSerializer;
    private JsonSerializer<RaceLogEvent> startTimeSerializer;
    private JsonSerializer<RaceLogEvent> raceStatusSerializer;
    private JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> passChangedSerializer;
    private JsonSerializer<RaceLogEvent> courseDesignChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> finishPositioningListChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> finishPositioningConfirmedEventSerializer;
    private JsonSerializer<RaceLogEvent> pathfinderEventSerializer;
    private JsonSerializer<RaceLogEvent> gateLineOpeningTimeEventSerializer;
    private JsonSerializer<RaceLogEvent> startProcedureTypeChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> protestStartTimeEventSerializer;

    private RaceLogEventFactory factory;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        flagEventSerializer = mock(JsonSerializer.class);
        startTimeSerializer = mock(JsonSerializer.class);
        raceStatusSerializer = mock(JsonSerializer.class);
        courseAreaChangedEventSerializer = mock(JsonSerializer.class);
        passChangedSerializer = mock(JsonSerializer.class);
        courseDesignChangedEventSerializer = mock(JsonSerializer.class);
        finishPositioningListChangedEventSerializer = mock(JsonSerializer.class);
        finishPositioningConfirmedEventSerializer = mock(JsonSerializer.class);
        pathfinderEventSerializer = mock(JsonSerializer.class);
        gateLineOpeningTimeEventSerializer = mock(JsonSerializer.class);
        startProcedureTypeChangedEventSerializer = mock(JsonSerializer.class);
        protestStartTimeEventSerializer = mock(JsonSerializer.class);

        serializer = new RaceLogEventSerializer(flagEventSerializer, startTimeSerializer, raceStatusSerializer,
                courseAreaChangedEventSerializer, passChangedSerializer, courseDesignChangedEventSerializer,
                finishPositioningListChangedEventSerializer, finishPositioningConfirmedEventSerializer,
                pathfinderEventSerializer, gateLineOpeningTimeEventSerializer,
                startProcedureTypeChangedEventSerializer, protestStartTimeEventSerializer);

        factory = RaceLogEventFactory.INSTANCE;
    }

    @Test
    public void testChoosesFlagEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFlagEvent(null, 0, null, null, false);
        serializer.serialize(event);
        verify(flagEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesStartTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createStartTimeEvent(null, 0, null);
        serializer.serialize(event);
        verify(startTimeSerializer).serialize(event);
    }

    @Test
    public void testChoosesRaceStatusSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createRaceStatusEvent(null, 0, RaceLogRaceStatus.SCHEDULED);
        serializer.serialize(event);
        verify(raceStatusSerializer).serialize(event);
    }

    @Test
    public void testChoosesCourseAreaChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createCourseAreaChangedEvent(null, 0, null);
        serializer.serialize(event);
        verify(courseAreaChangedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesPassChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createPassChangeEvent(null, 0);
        serializer.serialize(event);
        verify(passChangedSerializer).serialize(event);
    }

    @Test
    public void testChoosesCourseDesignChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createCourseDesignChangedEvent(null, 0, null);
        serializer.serialize(event);
        verify(courseDesignChangedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesFinishPositioningListChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFinishPositioningListChangedEvent(null, 0, null);
        serializer.serialize(event);
        verify(finishPositioningListChangedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesFinishPositioningConfirmedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFinishPositioningConfirmedEvent(null, 0);
        serializer.serialize(event);
        verify(finishPositioningConfirmedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesPathfinderSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createPathfinderEvent(null, 0, "GER 20");
        serializer.serialize(event);
        verify(pathfinderEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesGateLineOpeningTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createGateLineOpeningTimeEvent(null, 0, new Long(0));
        serializer.serialize(event);
        verify(gateLineOpeningTimeEventSerializer).serialize(event);
    }

    @Test
    public void testStartProcedureTypeChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createStartProcedureChangedEvent(null, 0, StartProcedureType.ESS);
        serializer.serialize(event);
        verify(startProcedureTypeChangedEventSerializer).serialize(event);
    }
    
    @Test
    public void testProtestStartTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createProtestStartTimeEvent(null, 0, null);
        serializer.serialize(event);
        verify(protestStartTimeEventSerializer).serialize(event);
    }

}
