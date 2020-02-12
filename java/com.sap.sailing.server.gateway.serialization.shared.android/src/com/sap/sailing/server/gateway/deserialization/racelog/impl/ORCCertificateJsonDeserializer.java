package com.sap.sailing.server.gateway.deserialization.racelog.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.ORCCertificateJsonSerializer;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.SecondsDurationImpl;

public class ORCCertificateJsonDeserializer implements JsonDeserializer<ORCCertificate> {

    @Override
    public ORCCertificate deserialize(JSONObject json) throws JsonDeserializationException {
        String sailnumber = (String) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_SAILNUMBER);
        String boatName = (String) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_BOATNAME);
        String boatclass = (String) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_BOATCLASS);
        Distance length = new MeterDistance(
                ((Number) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_LENGTH)).doubleValue());
        Duration gph = new SecondsDurationImpl(
                ((Number) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_GPH)).doubleValue());
        final Number cdlAsNumber = (Number) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_CDL);
        Double cdl = cdlAsNumber == null ? null : cdlAsNumber.doubleValue();
        TimePoint issueDate = new MillisecondsTimePoint((long) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_ISSUE_DATE));
        Map<Speed, Map<Bearing, Speed>> velocityPredictionsPerTrueWindSpeedAndAngle = new HashMap<>();
        Map<Speed, Bearing> beatAngles = new HashMap<>();
        Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed = new HashMap<>();
        Map<Speed, Duration> beatAllowancePerTrueWindSpeed = new HashMap<>();
        Map<Speed, Bearing> runAngles = new HashMap<>();
        Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed = new HashMap<>();
        Map<Speed, Duration> runAllowancePerTrueWindSpeed = new HashMap<>();
        Map<Speed, Speed> windwardLeewardSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        Map<Speed, Speed> longDistanceSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        Map<Speed, Speed> circularRandomSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        Map<Speed, Speed> nonSpinnakerSpeedPredictionPerTrueWindSpeed = new HashMap<>();
        Speed[] trueWindSpeeds = convertJsonArrayOfDoublesToArrayOfObjectsOrReturnDefault(json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_TRUE_WIND_SPEEDS_IN_KNOTS),
                ORCCertificate.ALLOWANCES_TRUE_WIND_SPEEDS, speedInKnots->new KnotSpeedImpl(speedInKnots), new Speed[0]);
        Bearing[] trueWindAngles = convertJsonArrayOfDoublesToArrayOfObjectsOrReturnDefault(json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_TRUE_WIND_ANGLES_IN_TRUE_DEGREES),
                ORCCertificate.ALLOWANCES_TRUE_WIND_ANGLES, twaInTrueDegrees->new DegreeBearingImpl(twaInTrueDegrees), new Bearing[0]);
        for (Speed tws : trueWindSpeeds) {
            String twsKey = ORCCertificateJsonSerializer.speedToKnotsString(tws);
            beatAngles.put(tws, new DegreeBearingImpl(
                    ((Number) ((JSONObject) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_BEAT_ANGLES))
                            .get(twsKey)).doubleValue()));
            beatVMGPredictionPerTrueWindSpeed.put(tws,
                    new KnotSpeedImpl(((Number) ((JSONObject) json
                            .get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_BEAT_VMG_PREDICTIONS)).get(twsKey))
                                    .doubleValue()));
            beatAllowancePerTrueWindSpeed.put(tws, new SecondsDurationImpl(
                    ((Number) ((JSONObject) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_BEAT_ALLOWANCES))
                            .get(twsKey)).doubleValue()));
            runAngles.put(tws, new DegreeBearingImpl(
                    ((Number) ((JSONObject) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_RUN_ANGLES))
                            .get(twsKey)).doubleValue()));
            runVMGPredictionPerTrueWindSpeed.put(tws,
                    new KnotSpeedImpl(((Number) ((JSONObject) json
                            .get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_RUN_VMG_PREDICTIONS)).get(twsKey))
                                    .doubleValue()));
            runAllowancePerTrueWindSpeed.put(tws, new SecondsDurationImpl(
                    ((Number) ((JSONObject) json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_RUN_ALLOWANCES))
                            .get(twsKey)).doubleValue()));
            windwardLeewardSpeedPredictionPerTrueWindSpeed.put(tws,
                    new KnotSpeedImpl(((Number) ((JSONObject) json
                            .get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_WINDWARD_LEEWARD_SPEED_PREDICTIONS))
                            .get(twsKey)).doubleValue()));
            longDistanceSpeedPredictionPerTrueWindSpeed.put(tws,
                    new KnotSpeedImpl(((Number) ((JSONObject) json
                            .get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_LONG_DISTANCE_SPEED_PREDICTIONS))
                            .get(twsKey)).doubleValue()));
            circularRandomSpeedPredictionPerTrueWindSpeed.put(tws,
                    new KnotSpeedImpl(((Number) ((JSONObject) json
                            .get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_CIRCULAR_RANDOM_SPEED_PREDICTIONS))
                                    .get(twsKey)).doubleValue()));
            nonSpinnakerSpeedPredictionPerTrueWindSpeed.put(tws,
                    new KnotSpeedImpl(((Number) ((JSONObject) json
                            .get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_NON_SPINNAKER_SPEED_PREDICTIONS))
                                    .get(twsKey)).doubleValue()));
            Map<Bearing, Speed> velocityPredictionAtCurrentTrueWindSpeedPerTrueWindAngle = new HashMap<>();
            for (Bearing twa : trueWindAngles) {
                String twaKey = ORCCertificateJsonSerializer.bearingToDegreeString(twa);
                velocityPredictionAtCurrentTrueWindSpeedPerTrueWindAngle.put(twa,
                        new KnotSpeedImpl(((Number) ((JSONObject) ((JSONObject) json
                                .get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_TWA_SPEED_PREDICTIONS)).get(twsKey))
                                        .get(twaKey)).doubleValue()));
            }
            velocityPredictionsPerTrueWindSpeedAndAngle.put(tws,
                    velocityPredictionAtCurrentTrueWindSpeedPerTrueWindAngle);
        }
        final String idConsistingOfNatAuthCertNoAndBIN = json.get(ORCCertificateJsonSerializer.ORC_CERTIFICATE_ID).toString();
        final ORCCertificate certificate = new ORCCertificateImpl(trueWindSpeeds,trueWindAngles,idConsistingOfNatAuthCertNoAndBIN, sailnumber,
                boatName, boatclass, length, gph, cdl, issueDate, velocityPredictionsPerTrueWindSpeedAndAngle,
                beatAngles, beatVMGPredictionPerTrueWindSpeed, beatAllowancePerTrueWindSpeed, runAngles,
                runVMGPredictionPerTrueWindSpeed, runAllowancePerTrueWindSpeed,
                windwardLeewardSpeedPredictionPerTrueWindSpeed, longDistanceSpeedPredictionPerTrueWindSpeed,
                circularRandomSpeedPredictionPerTrueWindSpeed, nonSpinnakerSpeedPredictionPerTrueWindSpeed);
        return certificate;
    }

    private <T> T[] convertJsonArrayOfDoublesToArrayOfObjectsOrReturnDefault(final Object supposedJsonArray,
            final T[] defaults, final Function<Double, T> constructor, T[] array) {
        final T[] result;
        final JSONArray windAnglesJsonArray;
        if (supposedJsonArray == null || !(supposedJsonArray instanceof JSONArray) || (windAnglesJsonArray=(JSONArray) supposedJsonArray).isEmpty()) {
            result = defaults;
        } else {
            final List<T> resultList = new ArrayList<>();
            for (final Object number : windAnglesJsonArray) {
                resultList.add(constructor.apply((Double) number));
            }
            final T[] tArray = resultList.toArray(array);
            result = tArray;
        }
        return result;
    }
}
