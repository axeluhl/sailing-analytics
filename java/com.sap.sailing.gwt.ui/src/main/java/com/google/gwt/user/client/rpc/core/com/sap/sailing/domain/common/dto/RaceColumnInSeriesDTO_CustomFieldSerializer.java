package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.dto;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;

public class RaceColumnInSeriesDTO_CustomFieldSerializer extends CustomFieldSerializer<RaceColumnInSeriesDTO> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public RaceColumnInSeriesDTO instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static RaceColumnInSeriesDTO instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new RaceColumnInSeriesDTO(streamReader.readString(), streamReader.readString());
    }

    public static void deserialize(SerializationStreamReader streamReader, RaceColumnInSeriesDTO instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, RaceColumnInSeriesDTO instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, RaceColumnInSeriesDTO instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, RaceColumnInSeriesDTO instance)
            throws SerializationException {
        streamWriter.writeString(instance.getSeriesName());
        streamWriter.writeString(instance.getRegattaName());
    }

}
