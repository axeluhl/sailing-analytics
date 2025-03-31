package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.confidence.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.confidence.impl.LazyDividedScaledPosition;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalablePosition;

public final class LazyDividedScaledPosition_CustomFieldSerializer extends
        CustomFieldSerializer<com.sap.sailing.domain.common.confidence.impl.LazyDividedScaledPosition> {
    public static void deserialize(SerializationStreamReader streamReader, LazyDividedScaledPosition instance)
            throws SerializationException {
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, LazyDividedScaledPosition instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, LazyDividedScaledPosition instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, LazyDividedScaledPosition instance)
            throws SerializationException {
        streamWriter.writeDouble(instance.getLatDeg());
        streamWriter.writeDouble(instance.getLngDeg());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public LazyDividedScaledPosition instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static LazyDividedScaledPosition instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final double latDeg = streamReader.readDouble();
        final double lngDeg = streamReader.readDouble();
        Position position = new DegreePosition(latDeg, lngDeg);
        ScalablePosition scalablePosition = new ScalablePosition(position);
        return new LazyDividedScaledPosition(scalablePosition, 1);
    }
}
