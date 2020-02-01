package com.sap.sailing.server.gateway.serialization.racelog.impl;

import java.text.MessageFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;

public class ORCCertificateJsonSerializer implements JsonSerializer<ORCCertificate> {
    public static final String ORC_CERTIFICATE_BEAT_ANGLES = "beatAngles";
    public static final String ORC_CERTIFICATE_RUN_ANGLES = "runAngles";
    public static final String ORC_CERTIFICATE_BEAT_ALLOWANCES = "beatAllowances";
    public static final String ORC_CERTIFICATE_RUN_ALLOWANCES = "runAllowances";
    public static final String ORC_CERTIFICATE_BEAT_VMG_PREDICTIONS = "beatVmgPredictions";
    public static final String ORC_CERTIFICATE_RUN_VMG_PREDICTIONS = "runVmgPredictions";
    public static final String ORC_CERTIFICATE_WINDWARD_LEEWARD_SPEED_PREDICTIONS = "windwardLeewardSpeedPredictions";
    public static final String ORC_CERTIFICATE_LONG_DISTANCE_SPEED_PREDICTIONS = "longDistanceSpeedPredictions";
    public static final String ORC_CERTIFICATE_CIRCULAR_RANDOM_SPEED_PREDICTIONS = "circularRandomSpeedPredictions";
    public static final String ORC_CERTIFICATE_NON_SPINNAKER_SPEED_PREDICTIONS = "nonSpinnakerSpeedPredictions";
    public static final String ORC_CERTIFICATE_TWA_SPEED_PREDICTIONS = "twaSpeedPredictions";
    public static final String ORC_CERTIFICATE_SAILNUMBER = "sailNumber";
    public static final String ORC_CERTIFICATE_BOATNAME = "boatName";
    public static final String ORC_CERTIFICATE_BOATCLASS = "boatClass";
    public static final String ORC_CERTIFICATE_GPH = "gph";
    public static final String ORC_CERTIFICATE_CDL = "cdl";
    public static final String ORC_CERTIFICATE_ID = "ID";
    public static final String ORC_CERTIFICATE_LENGTH = "length";
    public static final String ORC_CERTIFICATE_ISSUE_DATE = "issueDate";
    public static final String ORC_CERTIFICATE_TWS_KNOTS = "ORC_CERTIFICATE_TWS_{0}KT";
    public static final String ORC_CERTIFICATE_TWS_6KT = "ORC_CERTIFICATE_TWS_6KT";
    public static final String ORC_CERTIFICATE_TWS_8KT = "ORC_CERTIFICATE_TWS_8KT";
    public static final String ORC_CERTIFICATE_TWS_10KT = "ORC_CERTIFICATE_TWS_10KT";
    public static final String ORC_CERTIFICATE_TWS_12KT = "ORC_CERTIFICATE_TWS_12KT";
    public static final String ORC_CERTIFICATE_TWS_14KT = "ORC_CERTIFICATE_TWS_14KT";
    public static final String ORC_CERTIFICATE_TWS_16KT = "ORC_CERTIFICATE_TWS_16KT";
    public static final String ORC_CERTIFICATE_TWS_20KT = "ORC_CERTIFICATE_TWS_20KT";

    public static final String ORC_CERTIFICATE_R_PERDICTION = "ORC_CERTIFICATE_R{0}_PREDICTION";
    public static final String ORC_CERTIFICATE_R52_PREDICTION = "ORC_CERTIFICATE_R52_PREDICTION";
    public static final String ORC_CERTIFICATE_R60_PREDICTION = "ORC_CERTIFICATE_R60_PREDICTION";
    public static final String ORC_CERTIFICATE_R75_PREDICTION = "ORC_CERTIFICATE_R75_PREDICTION";
    public static final String ORC_CERTIFICATE_R90_PREDICTION = "ORC_CERTIFICATE_R90_PREDICTION";
    public static final String ORC_CERTIFICATE_R110_PREDICTION = "ORC_CERTIFICATE_R110_PREDICTION";
    public static final String ORC_CERTIFICATE_R120_PREDICTION = "ORC_CERTIFICATE_R120_PREDICTION";
    public static final String ORC_CERTIFICATE_R135_PREDICTION = "ORC_CERTIFICATE_R135_PREDICTION";
    public static final String ORC_CERTIFICATE_R150_PREDICTION = "ORC_CERTIFICATE_R150_PREDICTION";
    public static final String ORC_CERTIFICATE_TRUE_WIND_SPEED = "trueWindSpeeds";
    public static final String ORC_CERTIFICATE_TRUE_WIND_ANGLE = "trueWindAngles";

    @Override
    public JSONObject serialize(ORCCertificate certificate) {
        JSONObject result = new JSONObject();
        result.put(ORC_CERTIFICATE_ID, certificate.getId());
        result.put(ORC_CERTIFICATE_SAILNUMBER, certificate.getSailNumber());
        result.put(ORC_CERTIFICATE_BOATNAME, certificate.getBoatName());
        result.put(ORC_CERTIFICATE_BOATCLASS, certificate.getBoatClassName());
        result.put(ORC_CERTIFICATE_GPH, certificate.getGPHInSecondsToTheMile());
        result.put(ORC_CERTIFICATE_CDL, certificate.getCDL());
        result.put(ORC_CERTIFICATE_LENGTH, certificate.getLengthOverAll().getMeters());
        result.put(ORC_CERTIFICATE_ISSUE_DATE,
                certificate.getIssueDate() == null ? null : certificate.getIssueDate().asMillis());

        JSONObject beatAngles = new JSONObject();
        JSONObject runAngles = new JSONObject();
        JSONObject beatAllowances = new JSONObject();
        JSONObject runAllowances = new JSONObject();
        JSONObject beatVMGPredictions = new JSONObject();
        JSONObject runVMGPredictions = new JSONObject();
        JSONObject windwardLeewardPredictions = new JSONObject();
        JSONObject circularRandomPredictions = new JSONObject();
        JSONObject longDistancePredictions = new JSONObject();
        JSONObject nonSpinnakerPredictions = new JSONObject();
        JSONObject velocityPredictionsPerTrueWindSpeedAndAngle = new JSONObject();
        for (Speed tws : certificate.getTrueWindSpeeds()) {
            String keyTWS = speedToKnotsString(tws);
            JSONObject velocityPredictionsPerTrueWindAngle = new JSONObject();
            beatAngles.put(keyTWS, certificate.getBeatAngles().get(tws).getDegrees());
            runAngles.put(keyTWS, certificate.getRunAngles().get(tws).getDegrees());
            beatAllowances.put(keyTWS, certificate.getBeatAllowances().get(tws).asSeconds());
            runAllowances.put(keyTWS, certificate.getRunAllowances().get(tws).asSeconds());
            beatVMGPredictions.put(keyTWS, certificate.getBeatVMGPredictions().get(tws).getKnots());
            runVMGPredictions.put(keyTWS, certificate.getRunVMGPredictions().get(tws).getKnots());
            windwardLeewardPredictions.put(keyTWS, certificate.getWindwardLeewardSpeedPrediction().get(tws).getKnots());
            circularRandomPredictions.put(keyTWS, certificate.getCircularRandomSpeedPredictions().get(tws).getKnots());
            longDistancePredictions.put(keyTWS, certificate.getLongDistanceSpeedPredictions().get(tws).getKnots());
            nonSpinnakerPredictions.put(keyTWS, certificate.getNonSpinnakerSpeedPredictions().get(tws).getKnots());
            for (Bearing twa : certificate.getTrueWindAngles()) {
                String keyTWA = bearingToDegreeString(twa);
                velocityPredictionsPerTrueWindAngle.put(keyTWA,
                        certificate.getVelocityPredictionPerTrueWindSpeedAndAngle().get(tws).get(twa).getKnots());
            }
            velocityPredictionsPerTrueWindSpeedAndAngle.put(keyTWS, velocityPredictionsPerTrueWindAngle);
        }

        result.put(ORC_CERTIFICATE_BEAT_ANGLES, beatAngles);
        result.put(ORC_CERTIFICATE_RUN_ANGLES, runAngles);
        result.put(ORC_CERTIFICATE_BEAT_ALLOWANCES, beatAllowances);
        result.put(ORC_CERTIFICATE_RUN_ALLOWANCES, runAllowances);
        result.put(ORC_CERTIFICATE_BEAT_VMG_PREDICTIONS, beatVMGPredictions);
        result.put(ORC_CERTIFICATE_RUN_VMG_PREDICTIONS, runVMGPredictions);
        result.put(ORC_CERTIFICATE_WINDWARD_LEEWARD_SPEED_PREDICTIONS, windwardLeewardPredictions);
        result.put(ORC_CERTIFICATE_LONG_DISTANCE_SPEED_PREDICTIONS, longDistancePredictions);
        result.put(ORC_CERTIFICATE_CIRCULAR_RANDOM_SPEED_PREDICTIONS, circularRandomPredictions);
        result.put(ORC_CERTIFICATE_NON_SPINNAKER_SPEED_PREDICTIONS, nonSpinnakerPredictions);
        result.put(ORC_CERTIFICATE_TWA_SPEED_PREDICTIONS, velocityPredictionsPerTrueWindSpeedAndAngle);
        result.put(ORC_CERTIFICATE_TRUE_WIND_ANGLE, convertTrueWindAnglesToJsonArray(certificate));
        result.put(ORC_CERTIFICATE_TRUE_WIND_SPEED, convertTrueWindSpeedsToJsonArray(certificate));
        return result;
    }

    private JSONArray convertTrueWindSpeedsToJsonArray(ORCCertificate certificate) {
        JSONArray jsonArray = new JSONArray();
        for( Speed speed : certificate.getTrueWindSpeeds()) {
            jsonArray.add(speed.getKnots());
        }
        return jsonArray;
    }

    private JSONArray convertTrueWindAnglesToJsonArray(ORCCertificate certificate) {
        JSONArray jsonArray = new JSONArray();
        for( Bearing bearing : certificate.getTrueWindAngles()) {
            jsonArray.add(bearing.getDegrees());
        }
        return jsonArray;
    }

    public static String speedToKnotsString(Speed speed) {
        String result = null;
        if (speed.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[0])) {
            result = ORC_CERTIFICATE_TWS_6KT;
        } else if (speed.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[1])) {
            result = ORC_CERTIFICATE_TWS_8KT;
        } else if (speed.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[2])) {
            result = ORC_CERTIFICATE_TWS_10KT;
        } else if (speed.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[3])) {
            result = ORC_CERTIFICATE_TWS_12KT;
        } else if (speed.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[4])) {
            result = ORC_CERTIFICATE_TWS_14KT;
        } else if (speed.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[5])) {
            result = ORC_CERTIFICATE_TWS_16KT;
        } else if (speed.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS[6])) {
            result = ORC_CERTIFICATE_TWS_20KT;
        } else {
            result = MessageFormat.format(ORC_CERTIFICATE_TWS_KNOTS, String.valueOf(speed.getKnots()));
        }
        return result;
    }
    
    public static String bearingToDegreeString(Bearing bearing) {
        String result = null;
        if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[0])) {
            result = ORC_CERTIFICATE_R52_PREDICTION;
        } else if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[1])) {
            result = ORC_CERTIFICATE_R60_PREDICTION;
        } else if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[2])) {
            result = ORC_CERTIFICATE_R75_PREDICTION;
        } else if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[3])) {
            result = ORC_CERTIFICATE_R90_PREDICTION;
        } else if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[4])) {
            result = ORC_CERTIFICATE_R110_PREDICTION;
        } else if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[5])) {
            result = ORC_CERTIFICATE_R120_PREDICTION;
        } else if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[6])) {
            result = ORC_CERTIFICATE_R135_PREDICTION;
        } else if (bearing.equals(ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES[7])) {
            result = ORC_CERTIFICATE_R150_PREDICTION;
        } else {
            result = MessageFormat.format(ORC_CERTIFICATE_R_PERDICTION, String.valueOf(bearing.getDegrees()));
        }
        return result;
    }
    
}
