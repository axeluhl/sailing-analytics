package com.sap.sse.util;

import java.io.ObjectInputStream;

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
class EnumMap<K extends Enum<K>, V> extends java.util.EnumMap<K, V> {
    private final Class<K> keyType;
    private transient K[] keyUniverse;
    private transient Object[] vals;
    private transient int size = 0;
    private static final long serialVersionUID = 458661240069192865L;
    
    EnumMap(Class<K> c) {
        super(c);
        keyType = null;
    }
    
    public K[] getKeyUniverse() {
        return keyUniverse;
    }

    public Object[] getVals() {
        return vals;
    }

    public int getSize() {
        return size;
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
        // Read in size (number of Mappings)
        int size = s.readInt();
        // Read the keys and values, and put the mappings in the HashMap
        for (int i = 0; i < size; i++) {
            s.readObject(); // key
            s.readObject(); // value
        }
    }
}