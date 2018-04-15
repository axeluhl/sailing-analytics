package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.BoatHullType;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverMainCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.impl.CompleteManeuverCurveWithEstimationDataImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverMainCurveWithEstimationDataImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.CompleteManeuverCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ManeuverMainCurveWithEstimationDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.PositionJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.WindJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompleteManeuverCurveWithEstimationDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DetailedBoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ManeuverMainCurveWithEstimationDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PositionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.WindJsonSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class EstimationDataSerializationDeserializationTest {

    private static final double DELTA = 0.000001;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");

    @Test
    public void testCompleteManeuverCurveWithEstimationData() throws ParseException, JsonDeserializationException {
        MillisecondsTimePoint timePointMainCurveBefore = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:24"));
        MillisecondsTimePoint timePointMainCurveAfter = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:34"));
        SpeedWithBearing speedWithBearingMainCurveBefore = new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(30));
        SpeedWithBearing speedWithBearingMainCurveAfter = new KnotSpeedWithBearingImpl(5.5, new DegreeBearingImpl(90));
        double directionChangeMainCurveInDegrees = 60;
        SpeedWithBearing lowestSpeed = new KnotSpeedWithBearingImpl(3, new DegreeBearingImpl(60.5));
        MillisecondsTimePoint lowestSpeedTimePoint = new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:29"));
        SpeedWithBearing highestSpeed = new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(65));
        MillisecondsTimePoint highestSpeedTimePoint = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:32"));
        double maxTurningRateInDegreesPerSecond = 11.2043;
        double avgTurningRateInDegreesPerSecond = 9;
        Bearing courseAtMaxTurningRate = new DegreeBearingImpl(70.2244242);
        MillisecondsTimePoint maxTurningRateTimePoint = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:33"));
        int gpsFixesCountMainCurve = 4;
        Distance distanceSailedWithinManeuverMainCurve = new MeterDistance(4.4);
        Distance distanceSailedWithinManeuverTowardMiddleAngleProjectionMainCurve = new MeterDistance(3.6);
        Distance distanceSailedIfNotManeuveringMainCurve = new MeterDistance(6.4);
        Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuveringMainCurve = new MeterDistance(5.7);
        ManeuverMainCurveWithEstimationData mainCurve = new ManeuverMainCurveWithEstimationDataImpl(
                timePointMainCurveBefore, timePointMainCurveAfter, speedWithBearingMainCurveBefore,
                speedWithBearingMainCurveAfter, directionChangeMainCurveInDegrees, lowestSpeed, lowestSpeedTimePoint,
                highestSpeed, highestSpeedTimePoint, maxTurningRateTimePoint, maxTurningRateInDegreesPerSecond,
                courseAtMaxTurningRate, distanceSailedWithinManeuverMainCurve,
                distanceSailedWithinManeuverTowardMiddleAngleProjectionMainCurve,
                distanceSailedIfNotManeuveringMainCurve,
                distanceSailedTowardMiddleAngleProjectionIfNotManeuveringMainCurve, avgTurningRateInDegreesPerSecond,
                gpsFixesCountMainCurve);

        MillisecondsTimePoint timePointBefore = new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:10"));
        MillisecondsTimePoint timePointAfter = new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:55"));
        SpeedWithBearing speedWithBearingBefore = new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(20));
        SpeedWithBearing speedWithBearingAfter = new KnotSpeedWithBearingImpl(7.5, new DegreeBearingImpl(101.1));
        double directionChangeInDegrees = 81.1;
        Speed maneuverLowestSpeed = new KnotSpeedImpl(2.9);
        SpeedWithBearing avgSpeedWithBearingBefore = new KnotSpeedWithBearingImpl(4.999, new DegreeBearingImpl(32.202));
        SpeedWithBearing avgSpeedWithBearingAfter = new KnotSpeedWithBearingImpl(5.3333, new DegreeBearingImpl(90.234));
        Duration durationFromPreviousManeuverEndToManeuverStart = new MillisecondsDurationImpl(10001);
        Duration durationFromManeuverEndToNextManeuverStart = new MillisecondsDurationImpl(20000);
        int gpsFixesCountFromPreviousManeuverEndToManeuverStart = 2;
        int gpsFixesCountFromManeuverEndToNextManeuverStart = 222;
        Distance distanceSailedWithinManeuver = new MeterDistance(77.4);
        Distance distanceSailedWithinManeuverTowardMiddleAngleProjection = new MeterDistance(66.6);
        Distance distanceSailedIfNotManeuvering = new MeterDistance(99.4);
        Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuvering = new MeterDistance(88.7);
        int gpsFixesCount = 133445;
        ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl curve = new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl(
                timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter,
                directionChangeInDegrees, maneuverLowestSpeed, avgSpeedWithBearingBefore,
                durationFromPreviousManeuverEndToManeuverStart, gpsFixesCountFromPreviousManeuverEndToManeuverStart,
                avgSpeedWithBearingAfter, durationFromManeuverEndToNextManeuverStart,
                gpsFixesCountFromManeuverEndToNextManeuverStart, distanceSailedWithinManeuver,
                distanceSailedWithinManeuverTowardMiddleAngleProjection, distanceSailedIfNotManeuvering,
                distanceSailedTowardMiddleAngleProjectionIfNotManeuvering, gpsFixesCount);

        MillisecondsTimePoint windTimePoint = new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:25"));
        SpeedWithBearing windSpeedWithBearing = new KnotSpeedWithBearingImpl(2, new DegreeBearingImpl(340));
        DegreePosition windPosition = new DegreePosition(54.325246, 10.148556);
        Wind wind = new WindImpl(windPosition, windTimePoint, windSpeedWithBearing);

        int jibingCount = 203;
        int tackingCount = 12345;
        boolean maneuverStartsByRunningAwayFromWind = true;
        Bearing relativeBearingToNextMarkBeforeManeuver = new DegreeBearingImpl(202.23);
        Bearing relativeBearingToNextMarkAfterManeuver = new DegreeBearingImpl(10.01);
        boolean markPassing = true;

        CompleteManeuverCurveWithEstimationData toSerialize = new CompleteManeuverCurveWithEstimationDataImpl(mainCurve,
                curve, wind, tackingCount, jibingCount, maneuverStartsByRunningAwayFromWind,
                relativeBearingToNextMarkBeforeManeuver, relativeBearingToNextMarkAfterManeuver, markPassing);
        CompleteManeuverCurveWithEstimationDataJsonSerializer serializer = new CompleteManeuverCurveWithEstimationDataJsonSerializer(
                new ManeuverMainCurveWithEstimationDataJsonSerializer(),
                new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer(),
                new WindJsonSerializer(new PositionJsonSerializer()));
        JSONObject json = serializer.serialize(toSerialize);
        CompleteManeuverCurveWithEstimationDataJsonDeserializer deserializer = new CompleteManeuverCurveWithEstimationDataJsonDeserializer(
                new ManeuverMainCurveWithEstimationDataJsonDeserializer(),
                new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer(),
                new WindJsonDeserializer(new PositionJsonDeserializer()));
        CompleteManeuverCurveWithEstimationData deserialized = deserializer.deserialize(json);

        assertEquals(tackingCount, deserialized.getTackingCount());
        assertEquals(jibingCount, deserialized.getJibingCount());
        assertEquals(maneuverStartsByRunningAwayFromWind, deserialized.isManeuverStartsByRunningAwayFromWind());
        assertEquals(markPassing, deserialized.isMarkPassing());
        assertEquals(relativeBearingToNextMarkBeforeManeuver,
                deserialized.getRelativeBearingToNextMarkBeforeManeuver());
        assertEquals(relativeBearingToNextMarkAfterManeuver, deserialized.getRelativeBearingToNextMarkAfterManeuver());
        assertEquals(windTimePoint, deserialized.getWind().getTimePoint());
        assertEquals(windPosition, deserialized.getWind().getPosition());
        assertEquals(windSpeedWithBearing.getBearing(), deserialized.getWind().getBearing());
        assertEquals(windSpeedWithBearing.getMetersPerSecond(), deserialized.getWind().getMetersPerSecond(), DELTA);

        assertEquals(timePointBefore, deserialized.getCurveWithUnstableCourseAndSpeed().getTimePointBefore());
        assertEquals(timePointAfter, deserialized.getCurveWithUnstableCourseAndSpeed().getTimePointAfter());
        assertEquals(speedWithBearingBefore,
                deserialized.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore());
        assertEquals(speedWithBearingAfter,
                deserialized.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter());
        assertEquals(directionChangeInDegrees,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees(), DELTA);
        assertEquals(maneuverLowestSpeed, deserialized.getCurveWithUnstableCourseAndSpeed().getLowestSpeed());
        assertEquals(avgSpeedWithBearingBefore,
                deserialized.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingBefore());
        assertEquals(avgSpeedWithBearingAfter,
                deserialized.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter());
        assertEquals(durationFromPreviousManeuverEndToManeuverStart,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDurationFromPreviousManeuverEndToManeuverStart());
        assertEquals(durationFromManeuverEndToNextManeuverStart,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDurationFromManeuverEndToNextManeuverStart());
        assertEquals(gpsFixesCountFromPreviousManeuverEndToManeuverStart, deserialized
                .getCurveWithUnstableCourseAndSpeed().getGpsFixesCountFromPreviousManeuverEndToManeuverStart());
        assertEquals(gpsFixesCountFromManeuverEndToNextManeuverStart,
                deserialized.getCurveWithUnstableCourseAndSpeed().getGpsFixesCountFromManeuverEndToNextManeuverStart());
        assertEquals(distanceSailedWithinManeuver,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDistanceSailedWithinManeuver());
        assertEquals(distanceSailedWithinManeuverTowardMiddleAngleProjection, deserialized
                .getCurveWithUnstableCourseAndSpeed().getDistanceSailedWithinManeuverTowardMiddleAngleProjection());
        assertEquals(distanceSailedIfNotManeuvering,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDistanceSailedIfNotManeuvering());
        assertEquals(distanceSailedTowardMiddleAngleProjectionIfNotManeuvering, deserialized
                .getCurveWithUnstableCourseAndSpeed().getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering());
        assertEquals(gpsFixesCount, deserialized.getCurveWithUnstableCourseAndSpeed().getGpsFixesCount());

        assertEquals(timePointMainCurveBefore, deserialized.getMainCurve().getTimePointBefore());
        assertEquals(timePointMainCurveAfter, deserialized.getMainCurve().getTimePointAfter());
        assertEquals(speedWithBearingMainCurveBefore, deserialized.getMainCurve().getSpeedWithBearingBefore());
        assertEquals(speedWithBearingMainCurveAfter, deserialized.getMainCurve().getSpeedWithBearingAfter());
        assertEquals(directionChangeMainCurveInDegrees, deserialized.getMainCurve().getDirectionChangeInDegrees(),
                DELTA);
        assertEquals(lowestSpeed, deserialized.getMainCurve().getLowestSpeed());
        assertEquals(lowestSpeedTimePoint, deserialized.getMainCurve().getLowestSpeedTimePoint());
        assertEquals(highestSpeed, deserialized.getMainCurve().getHighestSpeed());
        assertEquals(highestSpeedTimePoint, deserialized.getMainCurve().getHighestSpeedTimePoint());
        assertEquals(maxTurningRateInDegreesPerSecond,
                deserialized.getMainCurve().getMaxTurningRateInDegreesPerSecond(), DELTA);
        assertEquals(avgTurningRateInDegreesPerSecond,
                deserialized.getMainCurve().getAvgTurningRateInDegreesPerSecond(), DELTA);
        assertEquals(courseAtMaxTurningRate, deserialized.getMainCurve().getCourseAtMaxTurningRate());
        assertEquals(maxTurningRateTimePoint, deserialized.getMainCurve().getTimePointOfMaxTurningRate());
        assertEquals(gpsFixesCountMainCurve, deserialized.getMainCurve().getGpsFixesCount());
        assertEquals(distanceSailedWithinManeuverMainCurve,
                deserialized.getMainCurve().getDistanceSailedWithinManeuver());
        assertEquals(distanceSailedWithinManeuverTowardMiddleAngleProjectionMainCurve,
                deserialized.getMainCurve().getDistanceSailedWithinManeuverTowardMiddleAngleProjection());
        assertEquals(distanceSailedIfNotManeuveringMainCurve,
                deserialized.getMainCurve().getDistanceSailedIfNotManeuvering());
        assertEquals(distanceSailedTowardMiddleAngleProjectionIfNotManeuveringMainCurve,
                deserialized.getMainCurve().getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering());
    }

    @Test
    public void testCompleteManeuverCurveWithEstimationDataNullValues()
            throws ParseException, JsonDeserializationException {
        MillisecondsTimePoint timePointMainCurveBefore = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:24"));
        MillisecondsTimePoint timePointMainCurveAfter = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:34"));
        SpeedWithBearing speedWithBearingMainCurveBefore = new KnotSpeedWithBearingImpl(5, new DegreeBearingImpl(30));
        SpeedWithBearing speedWithBearingMainCurveAfter = new KnotSpeedWithBearingImpl(5.5, new DegreeBearingImpl(90));
        double directionChangeMainCurveInDegrees = 60;
        SpeedWithBearing lowestSpeed = new KnotSpeedWithBearingImpl(3, new DegreeBearingImpl(60.5));
        MillisecondsTimePoint lowestSpeedTimePoint = new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:29"));
        SpeedWithBearing highestSpeed = new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(65));
        MillisecondsTimePoint highestSpeedTimePoint = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:32"));
        double maxTurningRateInDegreesPerSecond = 11.2043;
        double avgTurningRateInDegreesPerSecond = 9;
        Bearing courseAtMaxTurningRate = new DegreeBearingImpl(70.2244242);
        MillisecondsTimePoint maxTurningRateTimePoint = new MillisecondsTimePoint(
                dateFormat.parse("06/23/2011-15:28:33"));
        int gpsFixesCountMainCurve = 0;
        Distance distanceSailedWithinManeuverMainCurve = null;
        Distance distanceSailedWithinManeuverTowardMiddleAngleProjectionMainCurve = null;
        Distance distanceSailedIfNotManeuveringMainCurve = null;
        Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuveringMainCurve = null;
        ManeuverMainCurveWithEstimationData mainCurve = new ManeuverMainCurveWithEstimationDataImpl(
                timePointMainCurveBefore, timePointMainCurveAfter, speedWithBearingMainCurveBefore,
                speedWithBearingMainCurveAfter, directionChangeMainCurveInDegrees, lowestSpeed, lowestSpeedTimePoint,
                highestSpeed, highestSpeedTimePoint, maxTurningRateTimePoint, maxTurningRateInDegreesPerSecond,
                courseAtMaxTurningRate, distanceSailedWithinManeuverMainCurve,
                distanceSailedWithinManeuverTowardMiddleAngleProjectionMainCurve,
                distanceSailedIfNotManeuveringMainCurve,
                distanceSailedTowardMiddleAngleProjectionIfNotManeuveringMainCurve, avgTurningRateInDegreesPerSecond,
                gpsFixesCountMainCurve);

        MillisecondsTimePoint timePointBefore = new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:10"));
        MillisecondsTimePoint timePointAfter = new MillisecondsTimePoint(dateFormat.parse("06/23/2011-15:28:55"));
        SpeedWithBearing speedWithBearingBefore = new KnotSpeedWithBearingImpl(6, new DegreeBearingImpl(20));
        SpeedWithBearing speedWithBearingAfter = new KnotSpeedWithBearingImpl(7.5, new DegreeBearingImpl(101.1));
        double directionChangeInDegrees = 81.1;
        Speed maneuverLowestSpeed = new KnotSpeedImpl(2.9);
        SpeedWithBearing avgSpeedWithBearingBefore = null;
        SpeedWithBearing avgSpeedWithBearingAfter = null;
        Duration durationFromPreviousManeuverEndToManeuverStart = null;
        Duration durationFromManeuverEndToNextManeuverStart = null;
        int gpsFixesCountFromPreviousManeuverEndToManeuverStart = 0;
        int gpsFixesCountFromManeuverEndToNextManeuverStart = 0;
        Distance distanceSailedWithinManeuver = null;
        Distance distanceSailedWithinManeuverTowardMiddleAngleProjection = null;
        Distance distanceSailedIfNotManeuvering = null;
        Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuvering = null;
        int gpsFixesCount = 133445;
        ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl curve = new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataImpl(
                timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter,
                directionChangeInDegrees, maneuverLowestSpeed, avgSpeedWithBearingBefore,
                durationFromPreviousManeuverEndToManeuverStart, gpsFixesCountFromPreviousManeuverEndToManeuverStart,
                avgSpeedWithBearingAfter, durationFromManeuverEndToNextManeuverStart,
                gpsFixesCountFromManeuverEndToNextManeuverStart, distanceSailedWithinManeuver,
                distanceSailedWithinManeuverTowardMiddleAngleProjection, distanceSailedIfNotManeuvering,
                distanceSailedTowardMiddleAngleProjectionIfNotManeuvering, gpsFixesCount);

        Wind wind = null;

        int jibingCount = 0;
        int tackingCount = 0;
        boolean maneuverStartsByRunningAwayFromWind = false;
        Bearing relativeBearingToNextMarkBeforeManeuver = null;
        Bearing relativeBearingToNextMarkAfterManeuver = null;
        boolean markPassing = false;

        CompleteManeuverCurveWithEstimationData toSerialize = new CompleteManeuverCurveWithEstimationDataImpl(mainCurve,
                curve, wind, tackingCount, jibingCount, maneuverStartsByRunningAwayFromWind,
                relativeBearingToNextMarkBeforeManeuver, relativeBearingToNextMarkAfterManeuver, markPassing);
        CompleteManeuverCurveWithEstimationDataJsonSerializer serializer = new CompleteManeuverCurveWithEstimationDataJsonSerializer(
                new ManeuverMainCurveWithEstimationDataJsonSerializer(),
                new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonSerializer(),
                new WindJsonSerializer(new PositionJsonSerializer()));
        JSONObject json = serializer.serialize(toSerialize);
        CompleteManeuverCurveWithEstimationDataJsonDeserializer deserializer = new CompleteManeuverCurveWithEstimationDataJsonDeserializer(
                new ManeuverMainCurveWithEstimationDataJsonDeserializer(),
                new ManeuverCurveWithUnstableCourseAndSpeedWithEstimationDataJsonDeserializer(),
                new WindJsonDeserializer(new PositionJsonDeserializer()));
        CompleteManeuverCurveWithEstimationData deserialized = deserializer.deserialize(json);

        assertEquals(tackingCount, deserialized.getTackingCount());
        assertEquals(jibingCount, deserialized.getJibingCount());
        assertEquals(maneuverStartsByRunningAwayFromWind, deserialized.isManeuverStartsByRunningAwayFromWind());
        assertEquals(markPassing, deserialized.isMarkPassing());
        assertEquals(relativeBearingToNextMarkBeforeManeuver,
                deserialized.getRelativeBearingToNextMarkBeforeManeuver());
        assertEquals(relativeBearingToNextMarkAfterManeuver, deserialized.getRelativeBearingToNextMarkAfterManeuver());
        assertEquals(wind, deserialized.getWind());

        assertEquals(timePointBefore, deserialized.getCurveWithUnstableCourseAndSpeed().getTimePointBefore());
        assertEquals(timePointAfter, deserialized.getCurveWithUnstableCourseAndSpeed().getTimePointAfter());
        assertEquals(speedWithBearingBefore,
                deserialized.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore());
        assertEquals(speedWithBearingAfter,
                deserialized.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter());
        assertEquals(directionChangeInDegrees,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees(), DELTA);
        assertEquals(maneuverLowestSpeed, deserialized.getCurveWithUnstableCourseAndSpeed().getLowestSpeed());
        assertEquals(avgSpeedWithBearingBefore,
                deserialized.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingBefore());
        assertEquals(avgSpeedWithBearingAfter,
                deserialized.getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter());
        assertEquals(durationFromPreviousManeuverEndToManeuverStart,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDurationFromPreviousManeuverEndToManeuverStart());
        assertEquals(durationFromManeuverEndToNextManeuverStart,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDurationFromManeuverEndToNextManeuverStart());
        assertEquals(gpsFixesCountFromPreviousManeuverEndToManeuverStart, deserialized
                .getCurveWithUnstableCourseAndSpeed().getGpsFixesCountFromPreviousManeuverEndToManeuverStart());
        assertEquals(gpsFixesCountFromManeuverEndToNextManeuverStart,
                deserialized.getCurveWithUnstableCourseAndSpeed().getGpsFixesCountFromManeuverEndToNextManeuverStart());
        assertEquals(distanceSailedWithinManeuver,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDistanceSailedWithinManeuver());
        assertEquals(distanceSailedWithinManeuverTowardMiddleAngleProjection, deserialized
                .getCurveWithUnstableCourseAndSpeed().getDistanceSailedWithinManeuverTowardMiddleAngleProjection());
        assertEquals(distanceSailedIfNotManeuvering,
                deserialized.getCurveWithUnstableCourseAndSpeed().getDistanceSailedIfNotManeuvering());
        assertEquals(distanceSailedTowardMiddleAngleProjectionIfNotManeuvering, deserialized
                .getCurveWithUnstableCourseAndSpeed().getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering());
        assertEquals(gpsFixesCount, deserialized.getCurveWithUnstableCourseAndSpeed().getGpsFixesCount());

        assertEquals(timePointMainCurveBefore, deserialized.getMainCurve().getTimePointBefore());
        assertEquals(timePointMainCurveAfter, deserialized.getMainCurve().getTimePointAfter());
        assertEquals(speedWithBearingMainCurveBefore, deserialized.getMainCurve().getSpeedWithBearingBefore());
        assertEquals(speedWithBearingMainCurveAfter, deserialized.getMainCurve().getSpeedWithBearingAfter());
        assertEquals(directionChangeMainCurveInDegrees, deserialized.getMainCurve().getDirectionChangeInDegrees(),
                DELTA);
        assertEquals(lowestSpeed, deserialized.getMainCurve().getLowestSpeed());
        assertEquals(lowestSpeedTimePoint, deserialized.getMainCurve().getLowestSpeedTimePoint());
        assertEquals(highestSpeed, deserialized.getMainCurve().getHighestSpeed());
        assertEquals(highestSpeedTimePoint, deserialized.getMainCurve().getHighestSpeedTimePoint());
        assertEquals(maxTurningRateInDegreesPerSecond,
                deserialized.getMainCurve().getMaxTurningRateInDegreesPerSecond(), DELTA);
        assertEquals(avgTurningRateInDegreesPerSecond,
                deserialized.getMainCurve().getAvgTurningRateInDegreesPerSecond(), DELTA);
        assertEquals(courseAtMaxTurningRate, deserialized.getMainCurve().getCourseAtMaxTurningRate());
        assertEquals(maxTurningRateTimePoint, deserialized.getMainCurve().getTimePointOfMaxTurningRate());
        assertEquals(gpsFixesCountMainCurve, deserialized.getMainCurve().getGpsFixesCount());
        assertEquals(distanceSailedWithinManeuverMainCurve,
                deserialized.getMainCurve().getDistanceSailedWithinManeuver());
        assertEquals(distanceSailedWithinManeuverTowardMiddleAngleProjectionMainCurve,
                deserialized.getMainCurve().getDistanceSailedWithinManeuverTowardMiddleAngleProjection());
        assertEquals(distanceSailedIfNotManeuveringMainCurve,
                deserialized.getMainCurve().getDistanceSailedIfNotManeuvering());
        assertEquals(distanceSailedTowardMiddleAngleProjectionIfNotManeuveringMainCurve,
                deserialized.getMainCurve().getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering());
    }

    @Test
    public void testDetailedBoatClass() throws JsonDeserializationException {
        String name = "bla";
        boolean typicallyStartsUpwind = true;
        String displayName = "A bla";
        Distance hullLength = new MeterDistance(11);
        Distance hullBeam = new MeterDistance(4.01);
        BoatHullType hullType = BoatHullType.SURFERBOARD;
        BoatClass boatClass = new BoatClassImpl(name, typicallyStartsUpwind, displayName, hullLength, hullBeam,
                hullType);
        DetailedBoatClassJsonSerializer serializer = new DetailedBoatClassJsonSerializer();
        JSONObject json = serializer.serialize(boatClass);
        DetailedBoatClassJsonDeserializer deserializer = new DetailedBoatClassJsonDeserializer();
        BoatClass deserialized = deserializer.deserialize(json);
        assertEquals(name, deserialized.getName());
        assertEquals(typicallyStartsUpwind, deserialized.typicallyStartsUpwind());
        assertEquals(displayName, deserialized.getDisplayName());
        assertEquals(hullLength, deserialized.getHullLength());
        assertEquals(hullBeam, deserialized.getHullBeam());
        assertEquals(hullType, deserialized.getHullType());
    }

}
