package com.google.gwt.user.client.rpc.core.com.sap.sse.datamining.shared.impl.dto;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

/**
 * Ensures that the {@link ClusterDTO#getLocalizedName() localizedName} field is serialized by
 * really evaluating it eagerly now; this way, an optional lazy evaluation function does not need
 * to be serialized.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ClusterDTO_CustomFieldSerializer extends CustomFieldSerializer<ClusterDTO> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, ClusterDTO instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, ClusterDTO instance)
            throws SerializationException {
        streamWriter.writeString(instance.getSignifier());
        streamWriter.writeString(instance.getLocalizedName());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public ClusterDTO instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    public static ClusterDTO instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new ClusterDTO(streamReader.readString(), streamReader.readString());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, ClusterDTO instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, ClusterDTO instance) {
        // Done by instantiateInstance
    }

}
