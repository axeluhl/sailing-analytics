package com.sap.sse.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.impl.RGBColor;

public class RGBColor_CustomFieldSerializer extends CustomFieldSerializer<RGBColor> {
    public static void serialize(SerializationStreamWriter streamWriter, RGBColor instance) throws SerializationException {
        streamWriter.writeInt(instance.getRed());
        streamWriter.writeInt(instance.getGreen());
        streamWriter.writeInt(instance.getBlue());
    }
    
    public static RGBColor instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final int red = streamReader.readInt();
        final int green = streamReader.readInt();
        final int blue = streamReader.readInt();
        return new RGBColor(red, green, blue);
    }
    
    public static boolean hasCustomInstantiate() {
        return true;
    }
    
    public static void deserialize(SerializationStreamReader streamReader, RGBColor instance)
            throws SerializationException {
        // no operation
    }
    
    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, RGBColor instance) throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    @Override
    public RGBColor instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }
    
    @Override
    public boolean hasCustomInstantiateInstance() {
        return hasCustomInstantiate();
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, RGBColor instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }
}
