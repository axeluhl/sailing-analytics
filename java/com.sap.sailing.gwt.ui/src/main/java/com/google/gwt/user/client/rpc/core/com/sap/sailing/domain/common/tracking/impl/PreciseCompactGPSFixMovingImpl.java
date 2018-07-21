package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.tracking.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixMovingImpl.PreciseCompactPosition;
import com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixMovingImpl.PreciseCompactSpeedWithBearing;
import com.sap.sse.common.impl.DegreeBearingImpl;

public final class PreciseCompactGPSFixMovingImpl {
    public static final class PreciseCompactPosition_CustomFieldSerializer extends CustomFieldSerializer<PreciseCompactPosition> {
        @Override
        public boolean hasCustomInstantiateInstance() {
            return true;
        }

        @Override
        public PreciseCompactPosition instantiateInstance(SerializationStreamReader streamReader)
                throws SerializationException {
            return instantiate(streamReader);
        }

        public static PreciseCompactPosition instantiate(SerializationStreamReader streamReader) throws SerializationException {
            final double latDeg = streamReader.readDouble();
            final double lngDeg = streamReader.readDouble();
            return (PreciseCompactPosition) new com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixMovingImpl(
                    new DegreePosition(latDeg, lngDeg), /* timePoint */null, /* speedWithBearing */ null).getPosition();
        }

        @Override
        public void deserializeInstance(SerializationStreamReader streamReader, PreciseCompactPosition instance)
                throws SerializationException {
            deserialize(streamReader, instance);
        }

        public static void deserialize(SerializationStreamReader streamReader, PreciseCompactPosition instance) {
            // handled by instantiate
        }

        @Override
        public void serializeInstance(SerializationStreamWriter streamWriter, PreciseCompactPosition instance)
                throws SerializationException {
            serialize(streamWriter, instance);
        }

        public static void serialize(SerializationStreamWriter streamWriter, PreciseCompactPosition instance)
                throws SerializationException {
            streamWriter.writeDouble(instance.getLatDeg());
            streamWriter.writeDouble(instance.getLngDeg());
        }
    }

    public static final class PreciseCompactSpeedWithBearing_CustomFieldSerializer extends CustomFieldSerializer<PreciseCompactSpeedWithBearing> {
        @Override
        public boolean hasCustomInstantiateInstance() {
            return true;
        }

        @Override
        public PreciseCompactSpeedWithBearing instantiateInstance(SerializationStreamReader streamReader)
                throws SerializationException {
            return instantiate(streamReader);
        }

        public static PreciseCompactSpeedWithBearing instantiate(SerializationStreamReader streamReader) throws SerializationException {
            final double speedInKnots = streamReader.readDouble();
            final double bearingDeg = streamReader.readDouble();
            return (PreciseCompactSpeedWithBearing) new com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixMovingImpl(
                    /* dummy position */ new DegreePosition(0, 0), /* timePoint */null, new KnotSpeedWithBearingImpl(speedInKnots, new DegreeBearingImpl(bearingDeg))).getSpeed();
        }

        @Override
        public void deserializeInstance(SerializationStreamReader streamReader, PreciseCompactSpeedWithBearing instance)
                throws SerializationException {
            deserialize(streamReader, instance);
        }

        public static void deserialize(SerializationStreamReader streamReader, PreciseCompactSpeedWithBearing instance) {
            // handled by instantiate
        }

        @Override
        public void serializeInstance(SerializationStreamWriter streamWriter, PreciseCompactSpeedWithBearing instance)
                throws SerializationException {
            serialize(streamWriter, instance);
        }

        public static void serialize(SerializationStreamWriter streamWriter, PreciseCompactSpeedWithBearing instance)
                throws SerializationException {
            streamWriter.writeDouble(instance.getKnots());
            streamWriter.writeDouble(instance.getBearing().getDegrees());
        }
    }
}