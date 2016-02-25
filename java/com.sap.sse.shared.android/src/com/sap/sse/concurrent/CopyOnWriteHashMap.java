package com.sap.sse.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link HashMap} that implements concurrent behavior for write operations by doing the changes in a copy of an inner map and swapping the inner map afterwards.
 * 
 * Read operations are in general non blocking. Be aware that the inner state could have changed on subsequent read operations.
 *
 * @param <K> The key object type
 * @param <V> The value object type
 */
public class CopyOnWriteHashMap<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = -5618926487507116463L;
    /**
     * Lock object use to guard write operations.
     */
    private transient NamedReentrantReadWriteLock lock;
    private Map<K, V> wrappedMap;
    
    public CopyOnWriteHashMap() {
        this("lock for CopyOnWriteHashMap");
    }
    
    public CopyOnWriteHashMap(String lockName) {
        lock = new NamedReentrantReadWriteLock(lockName, true);
        wrappedMap = new HashMap<>();
    }
    
    public CopyOnWriteHashMap(String lockName, Map<? extends K, ? extends V> initialValues) {
        lock = new NamedReentrantReadWriteLock(lockName, true);
        wrappedMap = new HashMap<>(initialValues);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(lock.getName());
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        final String lockName = (String) ois.readObject();
        if (lock == null) {
            lock = new NamedReentrantReadWriteLock(lockName, true);
        }
    }
    
    @Override
    public int size() {
        return wrappedMap.size();
    }

    @Override
    public boolean isEmpty() {
        return wrappedMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return wrappedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return wrappedMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return wrappedMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        LockUtil.lockForWrite(lock);
        try {
            final Map<K, V> copy = new HashMap<>(wrappedMap);
            final V result = copy.put(key, value);
            wrappedMap = copy;
            return result;
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public V remove(Object key) {
        LockUtil.lockForWrite(lock);
        try {
            final Map<K, V> copy = new HashMap<>(wrappedMap);
            final V result = copy.remove(key);
            wrappedMap = copy;
            return result;
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> values) {
        LockUtil.lockForWrite(lock);
        try {
            final Map<K, V> copy = new HashMap<>(wrappedMap);
            copy.putAll(values);
            wrappedMap = copy;
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public void clear() {
        LockUtil.lockForWrite(lock);
        try {
            wrappedMap = new HashMap<>();
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public Set<K> keySet() {
        return wrappedMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return wrappedMap.values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return wrappedMap.entrySet();
    }

    /**
     * Sets the values of this map to the values of the given map. When doing this outside of the scope of this Map, you
     * would typically clear the map and do a putAll. In concurrent cases this leads to a short time range where readers
     * see an empty map. This method ensures that there is only one swap of references so that there will always be a
     * consistent old or new state being seen by readers.
     */
    public void set(Map<? extends K, ? extends V> values) {
        LockUtil.lockForWrite(lock);
        try {
            if(values == null) {
                wrappedMap = new HashMap<>();
            } else {
                wrappedMap = new HashMap<>(values);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((wrappedMap == null) ? 0 : wrappedMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        CopyOnWriteHashMap other = (CopyOnWriteHashMap) obj;
        if (wrappedMap == null) {
            if (other.wrappedMap != null)
                return false;
        } else if (!wrappedMap.equals(other.wrappedMap))
            return false;
        return true;
    }
}
