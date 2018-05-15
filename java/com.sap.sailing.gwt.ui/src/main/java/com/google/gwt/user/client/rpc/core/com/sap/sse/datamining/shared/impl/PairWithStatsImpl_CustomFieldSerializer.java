package com.google.gwt.user.client.rpc.core.com.sap.sse.datamining.shared.impl;

import java.util.HashSet;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.shared.impl.PairWithStatsImpl;

@SuppressWarnings("rawtypes")
public class PairWithStatsImpl_CustomFieldSerializer extends CustomFieldSerializer<PairWithStatsImpl> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public PairWithStatsImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    @SuppressWarnings("unchecked")
    public static PairWithStatsImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final Pair average = (Pair) streamReader.readObject();
        final Pair min = (Pair) streamReader.readObject();
        final Pair max = (Pair) streamReader.readObject();
        final Pair median = (Pair) streamReader.readObject();
        final Pair standardDeviation = (Pair) streamReader.readObject();
        final HashSet individualPairs = (HashSet) streamReader.readObject();
        final String resultType = streamReader.readString();
        final long count = streamReader.readLong();
        return new PairWithStatsImpl<Object>(average, min, max, median, standardDeviation, individualPairs, count, resultType);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, PairWithStatsImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, PairWithStatsImpl instance) {
        // handled by instantiate
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, PairWithStatsImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, PairWithStatsImpl instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getAverage());
        streamWriter.writeObject(instance.getMin());
        streamWriter.writeObject(instance.getMax());
        streamWriter.writeObject(instance.getMedian());
        streamWriter.writeObject(instance.getStandardDeviation());
        streamWriter.writeObject(instance.getIndividualPairs());
        streamWriter.writeString(instance.getResultType());
        streamWriter.writeLong(instance.getCount());
    }
}
