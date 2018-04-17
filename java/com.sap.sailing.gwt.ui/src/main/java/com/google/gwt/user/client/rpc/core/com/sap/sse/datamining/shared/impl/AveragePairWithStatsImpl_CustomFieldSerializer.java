package com.google.gwt.user.client.rpc.core.com.sap.sse.datamining.shared.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.impl.AveragePairWithStatsImpl;

@SuppressWarnings("rawtypes")
public class AveragePairWithStatsImpl_CustomFieldSerializer extends CustomFieldSerializer<AveragePairWithStatsImpl> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public AveragePairWithStatsImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    @SuppressWarnings("unchecked")
    public static AveragePairWithStatsImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final Pair average = (Pair) streamReader.readObject();
        final Pair min = (Pair) streamReader.readObject();
        final Pair max = (Pair) streamReader.readObject();
        final Pair median = (Pair) streamReader.readObject();
        final Pair standardDeviation = (Pair) streamReader.readObject();
        final String resultType = streamReader.readString();
        final long count = streamReader.readLong();
        return new AveragePairWithStatsImpl<Object>(average, min, max, median, standardDeviation, count, resultType);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, AveragePairWithStatsImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, AveragePairWithStatsImpl instance) {
        // handled by instantiate
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, AveragePairWithStatsImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, AveragePairWithStatsImpl instance)
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
