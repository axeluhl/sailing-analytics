package com.sap.sse.common.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.sap.sse.common.impl.HSVColor;

public class HSVColor_CustomFieldSerializer extends CustomFieldSerializer<HSVColor> {
    public static void serialize(SerializationStreamWriter streamWriter, HSVColor instance) throws SerializationException {
        streamWriter.writeFloat(instance.getHue());
        streamWriter.writeFloat(instance.getSaturation());
        streamWriter.writeFloat(instance.getBrightness());
    }
    
    public static HSVColor instantiate(SerializationStreamReader streamReader) throws SerializationException {
        final float hue = streamReader.readFloat();
        final float saturation = streamReader.readFloat();
        final float brightness = streamReader.readFloat();
        return new HSVColor(hue, saturation, brightness);
    }
    
    public static boolean hasCustomInstantiate() {
        return true;
    }
    
    public static void deserialize(SerializationStreamReader streamReader, HSVColor instance)
            throws SerializationException {
        // no operation
    }
    
    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, HSVColor instance) throws SerializationException {
        serialize(streamWriter, instance);
    }
    
    @Override
    public HSVColor instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }
    
    @Override
    public boolean hasCustomInstantiateInstance() {
        return hasCustomInstantiate();
    }
    
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, HSVColor instance)
            throws SerializationException {
        deserialize(streamReader, instance);
    }
}
