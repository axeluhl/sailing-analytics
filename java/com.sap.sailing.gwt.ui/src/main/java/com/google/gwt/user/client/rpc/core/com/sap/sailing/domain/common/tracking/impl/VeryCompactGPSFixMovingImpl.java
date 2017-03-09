package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.tracking.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.tracking.impl.VeryCompactGPSFixMovingImpl.VeryCompactPosition;

public final class VeryCompactGPSFixMovingImpl {
    public static final class VeryCompactPosition_CustomFieldSerializer extends CustomFieldSerializer<VeryCompactPosition> {
        @Override
        public boolean hasCustomInstantiateInstance() {
            return true;
        }

        @Override
        public VeryCompactPosition instantiateInstance(SerializationStreamReader streamReader)
                throws SerializationException {
            return instantiate(streamReader);
        }

        public static VeryCompactPosition instantiate(SerializationStreamReader streamReader) throws SerializationException {
            final double latDeg = streamReader.readDouble();
            final double lngDeg = streamReader.readDouble();
            return (VeryCompactPosition) new com.sap.sailing.domain.common.tracking.impl.VeryCompactGPSFixImpl(
                    new DegreePosition(latDeg, lngDeg), /* timePoint */null).getPosition();
        }

        @Override
        public void deserializeInstance(SerializationStreamReader streamReader, VeryCompactPosition instance)
                throws SerializationException {
            deserialize(streamReader, instance);
        }

        public static void deserialize(SerializationStreamReader streamReader, VeryCompactPosition instance) {
            // handled by instantiate
        }

        @Override
        public void serializeInstance(SerializationStreamWriter streamWriter, VeryCompactPosition instance)
                throws SerializationException {
            serialize(streamWriter, instance);
        }

        public static void serialize(SerializationStreamWriter streamWriter, VeryCompactPosition instance)
                throws SerializationException {
            streamWriter.writeDouble(instance.getLatDeg());
            streamWriter.writeDouble(instance.getLngDeg());
        }
    }
}