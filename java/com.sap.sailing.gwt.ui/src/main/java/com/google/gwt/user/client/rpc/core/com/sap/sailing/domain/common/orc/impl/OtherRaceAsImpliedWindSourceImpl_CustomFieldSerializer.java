package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.orc.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.orc.impl.OtherRaceAsImpliedWindSourceImpl;
import com.sap.sse.common.Util.Triple;

public class OtherRaceAsImpliedWindSourceImpl_CustomFieldSerializer extends CustomFieldSerializer<OtherRaceAsImpliedWindSourceImpl> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, OtherRaceAsImpliedWindSourceImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, OtherRaceAsImpliedWindSourceImpl instance)
            throws SerializationException {
        streamWriter.writeObject(instance.getLeaderboardAndRaceColumnAndFleetOfDefiningRace());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public OtherRaceAsImpliedWindSourceImpl instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static OtherRaceAsImpliedWindSourceImpl instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        @SuppressWarnings("unchecked")
        final Triple<String, String, String> otherRaceReference = (Triple<String, String, String>) streamReader.readObject();
        return new OtherRaceAsImpliedWindSourceImpl(otherRaceReference);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, OtherRaceAsImpliedWindSourceImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, OtherRaceAsImpliedWindSourceImpl instance) {
        // Done by instantiateInstance
    }
}
