package com.sap.sse.security.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import com.sap.sse.security.PreferenceConverter;

public class JavaIoSerializablePreferenceConverter<T> implements PreferenceConverter<T> {
    @Override
    public String toPreferenceString(T preference) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(preference);
            oos.flush();
            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T toPreferenceObject(String stringPreference) {
        try {
            return (T) new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(stringPreference)))
                    .readObject();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}