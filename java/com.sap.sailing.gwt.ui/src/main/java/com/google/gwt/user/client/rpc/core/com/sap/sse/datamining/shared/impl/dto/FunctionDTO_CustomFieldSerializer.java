package com.google.gwt.user.client.rpc.core.com.sap.sse.datamining.shared.impl.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

/**
 * Ensures that the {@link FunctionDTO#getLocalizedName() localizedName} field is serialized by
 * really evaluating it eagerly now; this way, an optional lazy evaluation function does not need
 * to be serialized.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FunctionDTO_CustomFieldSerializer extends CustomFieldSerializer<FunctionDTO> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, FunctionDTO instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, FunctionDTO instance)
            throws SerializationException {
        streamWriter.writeBoolean(instance.isDimension());
        streamWriter.writeString(instance.getFunctionName());
        streamWriter.writeString(instance.getSourceTypeName());
        streamWriter.writeString(instance.getReturnTypeName());
        streamWriter.writeObject(instance.getParameterTypeNames());
        streamWriter.writeString(instance.getDisplayName());
        streamWriter.writeInt(instance.getOrdinal());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public FunctionDTO instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    @SuppressWarnings("unchecked") // the cast to List<String> is the problem here
    public static FunctionDTO instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        return new FunctionDTO(/* isDimension */ streamReader.readBoolean(), /* function name */ streamReader.readString(),
                /* source type name */ streamReader.readString(), /* return type name */ streamReader.readString(),
                /* parameter type names */ (List<String>) streamReader.readObject(), /* display name */ streamReader.readString(),
                /* ordinal */ streamReader.readInt());
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, FunctionDTO instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, FunctionDTO instance) {
        // Done by instantiateInstance
    }

}
