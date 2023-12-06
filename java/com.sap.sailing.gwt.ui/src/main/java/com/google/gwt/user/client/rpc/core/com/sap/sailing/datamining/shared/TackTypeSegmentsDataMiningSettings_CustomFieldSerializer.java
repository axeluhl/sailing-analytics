package com.google.gwt.user.client.rpc.core.com.sap.sailing.datamining.shared;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sse.common.Duration;

public final class TackTypeSegmentsDataMiningSettings_CustomFieldSerializer extends CustomFieldSerializer<TackTypeSegmentsDataMiningSettings> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public TackTypeSegmentsDataMiningSettings instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static TackTypeSegmentsDataMiningSettings instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final Duration minimumTackTypeSegmentDuration = (Duration) streamReader.readObject();
        final Duration minimumDurationBetweenAdjacentTackTypeSegments = (Duration) streamReader.readObject();
        return new TackTypeSegmentsDataMiningSettings(minimumTackTypeSegmentDuration, minimumDurationBetweenAdjacentTackTypeSegments);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, TackTypeSegmentsDataMiningSettings instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, TackTypeSegmentsDataMiningSettings instance) {
        // handled by instantiate
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, TackTypeSegmentsDataMiningSettings instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, TackTypeSegmentsDataMiningSettings instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getMinimumTackTypeSegmentDuration());
        streamWriter.writeObject(instance.getMinimumDurationBetweenAdjacentTackTypeSegments());
    }
}
