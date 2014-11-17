package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventRestoreFactory;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
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
    private JsonSerializer<RaceLogEvent> windFixEventSerializer;
    private JsonSerializer<RaceLogEvent> deviceCompetitorMappingEventSerializer;
    private JsonSerializer<RaceLogEvent> deviceMarkMappingEventSerializer;
    private JsonSerializer<RaceLogEvent> denoteForTrackingEventSerializer;
    private JsonSerializer<RaceLogEvent> startTrackingEventSerializer;
    private JsonSerializer<RaceLogEvent> revokeEventSerializer;
    private JsonSerializer<RaceLogEvent> registerCompetitorEventSerializer;
    private JsonSerializer<RaceLogEvent> defineMarkEventSerializer;
    private JsonSerializer<RaceLogEvent> closeOpenEndedDeviceMappingEventSerializer;
    private JsonSerializer<RaceLogEvent> additionalScoringInformationSerializer;

    private RaceLogEventFactory factory;
    private AbstractLogEventAuthor author = new RaceLogEventAuthorImpl("Test Author", 1);

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
        windFixEventSerializer = mock(JsonSerializer.class);
        deviceCompetitorMappingEventSerializer = mock(JsonSerializer.class);
        deviceMarkMappingEventSerializer = mock(JsonSerializer.class);
        denoteForTrackingEventSerializer = mock(JsonSerializer.class);
        startTrackingEventSerializer = mock(JsonSerializer.class);
        revokeEventSerializer = mock(JsonSerializer.class);
        registerCompetitorEventSerializer = mock(JsonSerializer.class);
        defineMarkEventSerializer = mock(JsonSerializer.class);
        closeOpenEndedDeviceMappingEventSerializer = mock(JsonSerializer.class);
        additionalScoringInformationSerializer = mock(JsonSerializer.class);

        serializer = new RaceLogEventSerializer(flagEventSerializer, startTimeSerializer, raceStatusSerializer,
                courseAreaChangedEventSerializer, passChangedSerializer, courseDesignChangedEventSerializer,
                finishPositioningListChangedEventSerializer, finishPositioningConfirmedEventSerializer,
                pathfinderEventSerializer, gateLineOpeningTimeEventSerializer,
                startProcedureTypeChangedEventSerializer, protestStartTimeEventSerializer, windFixEventSerializer,
                deviceCompetitorMappingEventSerializer, deviceMarkMappingEventSerializer, denoteForTrackingEventSerializer,
                startTrackingEventSerializer, revokeEventSerializer, registerCompetitorEventSerializer, defineMarkEventSerializer,
                closeOpenEndedDeviceMappingEventSerializer, additionalScoringInformationSerializer);

        factory = RaceLogEventFactory.INSTANCE;
    }

    @Test
    public void testChoosesFlagEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFlagEvent(null, author, 0, null, null, false);
        serializer.serialize(event);
        verify(flagEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesStartTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createStartTimeEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(startTimeSerializer).serialize(event);
    }

    @Test
    public void testChoosesRaceStatusSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createRaceStatusEvent(null, author, 0, RaceLogRaceStatus.SCHEDULED);
        serializer.serialize(event);
        verify(raceStatusSerializer).serialize(event);
    }

    @Test
    public void testChoosesCourseAreaChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createCourseAreaChangedEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(courseAreaChangedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesPassChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createPassChangeEvent(null, author, 0);
        serializer.serialize(event);
        verify(passChangedSerializer).serialize(event);
    }

    @Test
    public void testChoosesCourseDesignChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createCourseDesignChangedEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(courseDesignChangedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesFinishPositioningListChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFinishPositioningListChangedEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(finishPositioningListChangedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesFinishPositioningConfirmedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFinishPositioningConfirmedEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(finishPositioningConfirmedEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesPathfinderSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createPathfinderEvent(null, author, 0, "GER 20");
        serializer.serialize(event);
        verify(pathfinderEventSerializer).serialize(event);
    }

    @Test
    public void testChoosesGateLineOpeningTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createGateLineOpeningTimeEvent(null, author, 0, 2l, 1l);
        serializer.serialize(event);
        verify(gateLineOpeningTimeEventSerializer).serialize(event);
    }

    @Test
    public void testStartProcedureTypeChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createStartProcedureChangedEvent(null, author, 0, RacingProcedureType.ESS);
        serializer.serialize(event);
        verify(startProcedureTypeChangedEventSerializer).serialize(event);
    }
    
    @Test
    public void testProtestStartTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createProtestStartTimeEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(protestStartTimeEventSerializer).serialize(event);
    }
    
    @Test
    public void testWindFixSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createWindFixEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(windFixEventSerializer).serialize(event);
    }
    
    @Test
    public void testDeviceCompetitorMappingSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createDeviceCompetitorMappingEvent(null, author, null, null, 0, null, null);
        serializer.serialize(event);
        verify(deviceCompetitorMappingEventSerializer).serialize(event);
    }
    
    @Test
    public void testDeviceMarkMappingSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createDeviceMarkMappingEvent(null, author, null, null, 0, null, null);
        serializer.serialize(event);
        verify(deviceMarkMappingEventSerializer).serialize(event);
    }
    
    @Test
    public void testDenoteForTrackingSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createDenoteForTrackingEvent(null, author, 0, "", null, null);
        serializer.serialize(event);
        verify(denoteForTrackingEventSerializer).serialize(event);
    }
    
    @Test
    public void testCreateRaceSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createStartTrackingEvent(null, author, 0);
        serializer.serialize(event);
        verify(startTrackingEventSerializer).serialize(event);
    }
    
    @Test
    public void testRevokeEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = RaceLogEventRestoreFactory.INSTANCE.createRevokeEvent(MillisecondsTimePoint.now(),
                author, MillisecondsTimePoint.now(), 0, 0, UUID.randomUUID(), "type", "short info", "reason");
        serializer.serialize(event);
        verify(revokeEventSerializer).serialize(event);
    }
    
    @Test
    public void testRegisterCompetitorEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createRegisterCompetitorEvent(null, author, 0,
        		DomainFactory.INSTANCE.getOrCreateCompetitor("comp", "comp", null, null, null));
        serializer.serialize(event);
        verify(registerCompetitorEventSerializer).serialize(event);
    }
    
    @Test
    public void testDefineMarkEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createDefineMarkEvent(null, author, 0, null);
        serializer.serialize(event);
        verify(defineMarkEventSerializer).serialize(event);
    }
    
    @Test
    public void testCloseOpenEndedDeviceMappingEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createCloseOpenEndedDeviceMappingEvent(null, author, 0, null, null);
        serializer.serialize(event);
        verify(closeOpenEndedDeviceMappingEventSerializer).serialize(event);
    }

}
