package com.google.gwt.user.client.rpc.core.com.sap.sailing.datamining.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.datamining.shared.FoilingSegmentsDataMiningSettings;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;

public final class FoilingSegmentsDataMiningSettings_CustomFieldSerializer extends CustomFieldSerializer<FoilingSegmentsDataMiningSettings> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public FoilingSegmentsDataMiningSettings instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static FoilingSegmentsDataMiningSettings instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final Duration minimumFoilingSegmentDuration = (Duration) streamReader.readObject();
        final Duration minimumDurationBetweenAdjacentFoilingSegments = (Duration) streamReader.readObject();
        final Speed minimumSpeedForFoiling = (Speed) streamReader.readObject();
        final Speed maximumSpeedNotFoiling = (Speed) streamReader.readObject();
        final Distance minimumRideHeight = (Distance) streamReader.readObject();
        return new FoilingSegmentsDataMiningSettings(minimumFoilingSegmentDuration, minimumDurationBetweenAdjacentFoilingSegments,
                minimumSpeedForFoiling, maximumSpeedNotFoiling, minimumRideHeight);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, FoilingSegmentsDataMiningSettings instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, FoilingSegmentsDataMiningSettings instance) {
        // handled by instantiate
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, FoilingSegmentsDataMiningSettings instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, FoilingSegmentsDataMiningSettings instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getMinimumFoilingSegmentDuration());
        streamWriter.writeObject(instance.getMinimumDurationBetweenAdjacentFoilingSegments());
        streamWriter.writeObject(instance.getMinimumSpeedForFoiling());
        streamWriter.writeObject(instance.getMaximumSpeedNotFoiling());
        streamWriter.writeObject(instance.getMinimumRideHeight());
    }
}
