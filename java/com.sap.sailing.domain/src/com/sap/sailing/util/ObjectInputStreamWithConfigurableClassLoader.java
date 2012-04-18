package com.sap.sailing.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

public class ObjectInputStreamWithConfigurableClassLoader extends ObjectInputStream {
    public ObjectInputStreamWithConfigurableClassLoader(InputStream in) throws IOException, StreamCorruptedException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        String className = classDesc.getName();
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }
}
