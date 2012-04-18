package com.sap.sailing.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

public class ObjectInputStreamWithConfigurableClassLoader extends ObjectInputStream {
    private final ClassLoader loader;

    public ObjectInputStreamWithConfigurableClassLoader(InputStream in, ClassLoader loader) throws IOException, StreamCorruptedException {
        super(in);
        assert loader != null;
        if (loader == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.loader = loader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        String className = classDesc.getName();
        return loader.loadClass(className);
    }
}
