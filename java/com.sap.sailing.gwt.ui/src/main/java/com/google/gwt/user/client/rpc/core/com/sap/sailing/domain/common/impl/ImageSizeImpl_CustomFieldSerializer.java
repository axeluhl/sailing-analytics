package com.google.gwt.user.client.rpc.core.com.sap.sailing.domain.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sailing.domain.common.impl.ImageSizeImpl;

public class ImageSizeImpl_CustomFieldSerializer extends CustomFieldSerializer<ImageSizeImpl> {
    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

    @Override
    public ImageSizeImpl instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static ImageSizeImpl instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new ImageSizeImpl(streamReader.readInt(), streamReader.readInt());
    }

    public static void deserialize(SerializationStreamReader streamReader, ImageSizeImpl instance)
            throws SerializationException {
        // handled by instantiateInstance
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, ImageSizeImpl instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, ImageSizeImpl instance)
            throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, ImageSizeImpl instance)
            throws SerializationException {
        streamWriter.writeDouble(instance.getWidth());
        streamWriter.writeDouble(instance.getHeight());
    }

}
