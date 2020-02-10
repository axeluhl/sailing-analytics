package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.orc.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.orc.impl.ORCCertificateImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public class ORCCertificateImpl_CustomFieldSerializer extends CustomFieldSerializer<ORCCertificateImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, ORCCertificateImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, ORCCertificateImpl instance)
            throws SerializationException {
        streamWriter.writeString(instance.getId());
        streamWriter.writeString(instance.getSailNumber());
        streamWriter.writeString(instance.getBoatName());
        streamWriter.writeString(instance.getBoatClassName());
        streamWriter.writeObject(instance.getLengthOverAll());
        streamWriter.writeObject(instance.getGPH());
        streamWriter.writeDouble(instance.getCDL());
        streamWriter.writeObject(instance.getIssueDate());
        streamWriter.writeObject(new HashMap<>(instance.getVelocityPredictionPerTrueWindSpeedAndAngle()));
        streamWriter.writeObject(new HashMap<>(instance.getBeatAngles()));
        streamWriter.writeObject(new HashMap<>(instance.getBeatVMGPredictions()));
        streamWriter.writeObject(new HashMap<>(instance.getBeatAllowances()));
        streamWriter.writeObject(new HashMap<>(instance.getRunAngles()));
        streamWriter.writeObject(new HashMap<>(instance.getRunVMGPredictions()));
        streamWriter.writeObject(new HashMap<>(instance.getRunAllowances()));
        streamWriter.writeObject(new HashMap<>(instance.getWindwardLeewardSpeedPrediction()));
        streamWriter.writeObject(new HashMap<>(instance.getLongDistanceSpeedPredictions()));
        streamWriter.writeObject(new HashMap<>(instance.getCircularRandomSpeedPredictions()));
        streamWriter.writeObject(new HashMap<>(instance.getNonSpinnakerSpeedPredictions()));
        streamWriter.writeObject(Arrays.asList(instance.getTrueWindAngles()));
        streamWriter.writeObject(Arrays.asList(instance.getTrueWindSpeeds()));
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public ORCCertificateImpl instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static ORCCertificateImpl instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        final String idConsistingOfNatAuthCertNoAndBIN = streamReader.readString();
        final String sailnumber = streamReader.readString();
        final String boatName = streamReader.readString();
        final String boatclass = streamReader.readString();
        final Distance length = (Distance) streamReader.readObject();
        final Duration gph = (Duration) streamReader.readObject();
        final Double cdl = streamReader.readDouble();
        final TimePoint issueDate = (TimePoint) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Map<Bearing, Speed>> velocityPredictionsPerTrueWindSpeedAndAngle = (Map<Speed, Map<Bearing, Speed>>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Bearing> beatAngles = (Map<Speed, Bearing>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Speed> beatVMGPredictionPerTrueWindSpeed = (Map<Speed, Speed>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Duration> beatAllowancePerTrueWindSpeed = (Map<Speed, Duration>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Bearing> runAngles = (Map<Speed, Bearing>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Speed> runVMGPredictionPerTrueWindSpeed = (Map<Speed, Speed>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Duration> runAllowancePerTrueWindSpeed = (Map<Speed, Duration>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Speed> windwardLeewardSpeedPredictionsPerTrueWindSpeed = (Map<Speed, Speed>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Speed> longDistanceSpeedPredictionsPerTrueWindSpeed = (Map<Speed, Speed>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Speed> circularRandomSpeedPredictionsPerTrueWindSpeed = (Map<Speed, Speed>) streamReader.readObject();
        @SuppressWarnings("unchecked")
        final Map<Speed, Speed> nonSpinnakerSpeedPredictionsPerTrueWindSpeed = (Map<Speed, Speed>) streamReader.readObject();
        
        final Bearing[] trueWindAngles = getTrueWindAnglesFromStream(streamReader);
        final Speed[] trueWindSpeeds = getTrueWindSpeedsFromStream(streamReader);
        
        return new ORCCertificateImpl(trueWindSpeeds,trueWindAngles,idConsistingOfNatAuthCertNoAndBIN, sailnumber, boatName, boatclass, length, gph,
                cdl, issueDate, velocityPredictionsPerTrueWindSpeedAndAngle, beatAngles,
                beatVMGPredictionPerTrueWindSpeed, beatAllowancePerTrueWindSpeed, runAngles,
                runVMGPredictionPerTrueWindSpeed, runAllowancePerTrueWindSpeed,
                windwardLeewardSpeedPredictionsPerTrueWindSpeed, longDistanceSpeedPredictionsPerTrueWindSpeed,
                circularRandomSpeedPredictionsPerTrueWindSpeed, nonSpinnakerSpeedPredictionsPerTrueWindSpeed);
    }

    private static Speed[] getTrueWindSpeedsFromStream(SerializationStreamReader streamReader) {
        Speed[] trueWindSpeeds = null;
        try {
            Object trueWindSpeedsObject = streamReader.readObject();
            if(trueWindSpeedsObject != null) {
                @SuppressWarnings("unchecked")
                List<Speed> trueWIndSpeedList =  (List<Speed>) trueWindSpeedsObject;
                trueWindSpeeds = new Speed[trueWIndSpeedList.size()];
                trueWIndSpeedList.toArray(trueWindSpeeds);
            }
        } catch (SerializationException e) {
            trueWindSpeeds = ORCCertificateImpl.ALLOWANCES_TRUE_WIND_SPEEDS;
        }
        return trueWindSpeeds;
    }

    private static Bearing[] getTrueWindAnglesFromStream(SerializationStreamReader streamReader) {
        Bearing[] trueWindAngles = null;
        try {
            Object trueWindAnglesObject = streamReader.readObject();
            if(trueWindAnglesObject != null) {
                @SuppressWarnings("unchecked")
                List<Bearing> trueWIndAnglesList =  (List<Bearing>) trueWindAnglesObject;
                trueWindAngles = new Bearing[trueWIndAnglesList.size()];
                trueWIndAnglesList.toArray(trueWindAngles);
            }
        } catch (SerializationException e) {
            trueWindAngles = ORCCertificateImpl.ALLOWANCES_TRUE_WIND_ANGLES;
        }
        return trueWindAngles;
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, ORCCertificateImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, ORCCertificateImpl instance) {
        // Done by instantiateInstance
    }

}
