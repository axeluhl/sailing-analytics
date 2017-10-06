package com.google.gwt.user.client.rpc.core.com.sap.sse.datamining.shared.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.datamining.shared.impl.AverageWithStatsImpl;

@SuppressWarnings("rawtypes")
public class AverageWithStatsImpl_CustomFieldSerializer extends CustomFieldSerializer<AverageWithStatsImpl> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public AverageWithStatsImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static AverageWithStatsImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final Object average = streamReader.readObject();
        final Object min = streamReader.readObject();
        final Object max = streamReader.readObject();
        final Object median = streamReader.readObject();
        final Object standardDeviation = streamReader.readObject();
        final String resultType = streamReader.readString();
        final long count = streamReader.readLong();
        return new AverageWithStatsImpl<Object>(average, min, max, median, standardDeviation, count, resultType);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, AverageWithStatsImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, AverageWithStatsImpl instance) {
        // handled by instantiate
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, AverageWithStatsImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, AverageWithStatsImpl instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getAverage());
        streamWriter.writeObject(instance.getMin());
        streamWriter.writeObject(instance.getMax());
        streamWriter.writeObject(instance.getMedian());
        streamWriter.writeObject(instance.getStandardDeviation());
        streamWriter.writeString(instance.getResultType());
        streamWriter.writeLong(instance.getCount());
    }
}
