package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.tracking.impl;

import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.tracking.impl.CompactWindImpl.CompactPosition;
import com.sap.sailing.domain.common.tracking.impl.CompactionNotPossibleException;

public final class CompactWindImpl {
    private static final Logger logger = Logger.getLogger(CompactWindImpl.class.getName());
    
    public static final class CompactPosition_CustomFieldSerializer extends CustomFieldSerializer<CompactPosition> {
        @Override
        public boolean hasCustomInstantiateInstance() {
            return true;
        }

        @Override
        public CompactPosition instantiateInstance(SerializationStreamReader streamReader)
                throws SerializationException {
            return instantiate(streamReader);
        }

        public static CompactPosition instantiate(SerializationStreamReader streamReader) throws SerializationException {
            final double latDeg = streamReader.readDouble();
            final double lngDeg = streamReader.readDouble();
            try {
                return (CompactPosition) new com.sap.sailing.domain.common.tracking.impl.CompactWindImpl(new WindImpl(
                        new DegreePosition(latDeg, lngDeg), /* timePoint */null, new KnotSpeedWithBearingImpl(0,
                                new DegreeBearingImpl(0)))).getPosition();
            } catch (CompactionNotPossibleException e) {
                logger.severe("Internal error: Cannot de-serialize compact position: "+e.getMessage()+"; throwing runtime exception");
                throw new RuntimeException(e);
            }
        }

        @Override
        public void deserializeInstance(SerializationStreamReader streamReader, CompactPosition instance)
                throws SerializationException {
            deserialize(streamReader, instance);
        }

        public static void deserialize(SerializationStreamReader streamReader, CompactPosition instance) {
            // handled by instantiate
        }

        @Override
        public void serializeInstance(SerializationStreamWriter streamWriter, CompactPosition instance)
                throws SerializationException {
            serialize(streamWriter, instance);
        }

        public static void serialize(SerializationStreamWriter streamWriter, CompactPosition instance)
                throws SerializationException {
            streamWriter.writeDouble(instance.getLatDeg());
            streamWriter.writeDouble(instance.getLngDeg());
        }
    }
}