package com.google.gwt.user.client.rpc.core.com.sap.sse.datamining.shared.impl.dto;

import java.util.HashMap;
import java.util.HashSet;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ReducedDimensionsDTO;

/**
 * Ensures that the {@link FunctionDTO#getLocalizedName() localizedName} field is serialized by
 * really evaluating it eagerly now; this way, an optional lazy evaluation function does not need
 * to be serialized.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class ReducedDimensionsDTO_CustomFieldSerializer extends CustomFieldSerializer<ReducedDimensionsDTO> {

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, ReducedDimensionsDTO instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    public static void serialize(SerializationStreamWriter streamWriter, ReducedDimensionsDTO instance)
            throws SerializationException {
        final HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> reducedDimensions = instance.getReducedDimensions();
        streamWriter.writeObject(reducedDimensions);
        final HashMap<FunctionDTO, FunctionDTO> fromOriginalToReducedDimension = new HashMap<>(instance.getFromOriginalToReducedDimension());
        streamWriter.writeObject(fromOriginalToReducedDimension);
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }
    
    @Override
    public ReducedDimensionsDTO instantiateInstance(SerializationStreamReader streamReader)
            throws SerializationException {
        return instantiate(streamReader);
    }

    @SuppressWarnings("unchecked") // the cast to List<String> is the problem here
    public static ReducedDimensionsDTO instantiate(SerializationStreamReader streamReader)
            throws SerializationException {
        final HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> reducedDimensions = (HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>>) streamReader.readObject();
        final HashMap<FunctionDTO, FunctionDTO> fromOriginalToReducedDimension = (HashMap<FunctionDTO, FunctionDTO>) streamReader.readObject();
        return new ReducedDimensionsDTO(reducedDimensions, fromOriginalToReducedDimension);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, ReducedDimensionsDTO instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, ReducedDimensionsDTO instance) {
        // Done by instantiateInstance
    }

}
