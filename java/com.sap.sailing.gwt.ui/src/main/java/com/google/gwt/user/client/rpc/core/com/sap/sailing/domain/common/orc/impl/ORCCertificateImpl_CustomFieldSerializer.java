package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.orc.impl;

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
        streamWriter.writeString(instance.getSailnumber());
        streamWriter.writeString(instance.getBoatName());
        streamWriter.writeString(instance.getBoatclass());
        streamWriter.writeObject(instance.getLengthOverAll());
        streamWriter.writeObject(instance.getGPH());
        streamWriter.writeDouble(instance.getCDL());
        streamWriter.writeObject(instance.getIssueDate());
        streamWriter.writeObject(instance.getVelocityPredictionPerTrueWindSpeedAndAngle());
        streamWriter.writeObject(instance.getBeatAngles());
        streamWriter.writeObject(instance.getBeatVMGPredictions());
        streamWriter.writeObject(instance.getBeatAllowances());
        streamWriter.writeObject(instance.getRunAngles());
        streamWriter.writeObject(instance.getRunVMGPredictions());
        streamWriter.writeObject(instance.getRunAllowances());
        streamWriter.writeObject(instance.getWindwardLeewardSpeedPrediction());
        streamWriter.writeObject(instance.getLongDistanceSpeedPredictions());
        streamWriter.writeObject(instance.getCircularRandomSpeedPredictions());
        streamWriter.writeObject(instance.getNonSpinnakerSpeedPredictions());
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
        return new ORCCertificateImpl(idConsistingOfNatAuthCertNoAndBIN, sailnumber, boatName, boatclass, length, gph,
                cdl, issueDate, velocityPredictionsPerTrueWindSpeedAndAngle, beatAngles,
                beatVMGPredictionPerTrueWindSpeed, beatAllowancePerTrueWindSpeed, runAngles,
                runVMGPredictionPerTrueWindSpeed, runAllowancePerTrueWindSpeed,
                windwardLeewardSpeedPredictionsPerTrueWindSpeed, longDistanceSpeedPredictionsPerTrueWindSpeed,
                circularRandomSpeedPredictionsPerTrueWindSpeed, nonSpinnakerSpeedPredictionsPerTrueWindSpeed);
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
