package com.sap.sse.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 * A serialization-compatible mock for {@link java.util.EnumMap} that uses an equal {@link #serialVersionUID} and a
 * compatible set of fields. When de-serializing an {@link java.util.EnumMap} instance into an instance of this class,
 * e.g., by overriding {@link ObjectInputStream}'s {@link ObjectInputStream#resolveClass} method such that it delivers
 * this class instead of {@link java.util.EnumMap}, the fields are made accessible through getters; in particular,
 * {@link #getKeyType()} reveals the original {@link java.util.EnumMap}'s key type, even if the map is empty.<p>
 * 
 * The {@link EnumMap#getEnumMapKeyType(java.util.EnumMap)} method can be used to determine the key type of any
 * {@link java.util.EnumMap}, even if it's empty.
 */
public class EnumMap<K extends Enum<K>, V> extends java.util.EnumMap<K, V> {
    private final Class<K> keyType;
    private transient K[] keyUniverse;
    private transient Object[] vals;
    private transient int size = 0;
    private static final long serialVersionUID = 458661240069192865L;
    
    private static class MyObjectInputStream extends ObjectInputStream {
        public MyObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            final Class<?> result;
            if (desc.getName().equals("java.util.EnumMap")) {
                result = com.sap.sse.test.EnumMap.class;
            } else {
                result = super.resolveClass(desc);
            }
            return result;
        }
    }
    
    public static Class<?> getEnumMapKeyType(java.util.EnumMap<?, ?> enumMap) throws IOException, ClassNotFoundException {
        final Class<?> result;
        if (enumMap.isEmpty()) {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(enumMap);
            oos.close();
            final ObjectInputStream ois = new MyObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            final com.sap.sse.test.EnumMap<?, ?> readMap = (com.sap.sse.test.EnumMap<?, ?>) ois.readObject();
            final Class<?> keyType = readMap.getKeyType();
            result = keyType;
        } else {
            result = enumMap.keySet().iterator().next().getDeclaringClass();
        }
        return result;
    }

    EnumMap(Class<K> c) {
        super(c);
        keyType = null;
    }
    
    public K[] getKeyUniverse() {
        return keyUniverse;
    }

    public void setKeyUniverse(K[] keyUniverse) {
        this.keyUniverse = keyUniverse;
    }

    public Object[] getVals() {
        return vals;
    }

    public void setVals(Object[] vals) {
        this.vals = vals;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Class<K> getKeyType() {
        return keyType;
    }

    /**
     * Reconstitute the <tt>EnumMap</tt> instance from a stream (i.e., deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // Read in the key type and any hidden stuff
        s.defaultReadObject();
        System.out.println(keyType);
        // Read in size (number of Mappings)
        int size = s.readInt();
        // Read the keys and values, and put the mappings in the HashMap
        for (int i = 0; i < size; i++) {
            s.readObject(); // key
            s.readObject(); // value
        }
    }
}