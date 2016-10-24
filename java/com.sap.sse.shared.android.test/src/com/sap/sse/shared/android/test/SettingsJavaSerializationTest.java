package com.sap.sse.shared.android.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;

public class SettingsJavaSerializationTest extends AbstractSettingsSerializationTest<byte[]> {

    @Override
    protected <T extends GenericSerializableSettings> byte[] serialize(T settings) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);) {
            oos.writeObject(settings);
            oos.close();
            return baos.toByteArray();
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends GenericSerializableSettings> T deserialize(byte[] serializedObject, Class<T> settingsClass) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
                ObjectInputStream ois = new ObjectInputStream(bais);) {
            return (T) ois.readObject();
        }

    }
}
