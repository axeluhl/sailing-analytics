package com.sap.sse.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sse.common.Base64Utils;
import com.sap.sse.shared.classloading.JoinedClassLoader;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

/**
 * Static class to (de)serialize {@link Serializable} objects from/to base64 strings.
 */
public class Base64SerializerDeserializer {
    private static final Logger LOG = Logger.getLogger(Base64SerializerDeserializer.class.getName());

    /** @return the {@code T} as a base64 string serialized with java serialization */
    public static <T extends Serializable> String toBase64(final T dto) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(stream)) {
            out.writeObject(dto);
            final byte[] bytes = stream.toByteArray();
            return Base64Utils.toBase64(bytes);
        } catch (IOException e) {
            LOG.warning("Could not serialize report: " + e.getMessage());
        }
        return "";
    }

    /** @return the {@code T} from a base 64 string deserialized with java serialization */
    public static <T extends Serializable> T fromBase64(final String data, final JoinedClassLoader classLoader) {
        final byte[] bytes;
        try {
            bytes = Base64Utils.fromBase64(data);
        } catch (IllegalArgumentException e) {
            return null;
        }
        final ClassLoader oldThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try (final ObjectInputStream in = new ObjectInputStreamResolvingAgainstCache<Object>(
                new ByteArrayInputStream(bytes), /* dummy "cache" */ new Object(), /* resolve listener */ null,
                /* classLoaderCache */ new HashMap<>()) {
        }) {
            @SuppressWarnings("unchecked")
            final T object = (T) in.readObject();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "Could not deserialize report", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldThreadContextClassLoader);
        }
        return null;
    }
}
