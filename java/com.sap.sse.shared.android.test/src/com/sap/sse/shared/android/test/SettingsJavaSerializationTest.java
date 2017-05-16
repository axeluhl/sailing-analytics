package com.sap.sse.shared.android.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;

public class SettingsJavaSerializationTest extends AbstractSettingsSerializationTest<byte[]> {

    @Override
    protected <T extends GenericSerializableSettings> byte[] serialize(T settings) throws Exception {
        return serializeObject(settings);
    }

    private byte[] serializeObject(Object settings) throws IOException {
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
        return (T) deserializeObject(serializedObject);

    }

    private Object deserializeObject(byte[] serializedObject) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedObject);
                ObjectInputStream ois = new ObjectInputStream(bais);) {
            return ois.readObject();
        }
    }
}
