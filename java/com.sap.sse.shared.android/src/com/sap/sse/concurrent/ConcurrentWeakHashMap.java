package com.sap.sse.concurrent;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.common.Util;

/**
 * A map that is concurrency-safe as provided by the {@link ConcurrentHashMap} implementation, and referencing
 * keys only weakly. Like {@link ConcurrentHashMap}, this map does not support <code>null</code> keys.
 * 
 * @author Axel Uhl (D043530)
 */
public class ConcurrentWeakHashMap<K, V> implements Map<K, V> {
    private final ConcurrentHashMap<WeakReferenceThatIsEqualToItselfAndItsReferent, V> map;
    
    private final ReferenceQueue<K> queue;
    
    /**
     * A reference whose hash code is based on the hash code of the original referent, also after the
     * reference has been enqueued. Equality is defined such that this reference is equal to itself which is
     * helpful to remove the entries from the {@link ConcurrentHashMap}, as well as equal to any other reference
     * of this type if their referents are equal.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class WeakReferenceThatIsEqualToItselfAndItsReferent extends WeakReference<K> {
        private final int hashCode;
        
        public WeakReferenceThatIsEqualToItselfAndItsReferent(K referent, ReferenceQueue<? super K> q) {
            super(referent, q);
            hashCode = referent.hashCode();
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        /**
         * If <code>other</code> is this reference, it it considered equal. Otherwise, <code>other</code> is considered
         * equal to this reference if the referent is still set and the referent equals <code>other</code>.
         */
        @Override
        public boolean equals(Object other) {
            final boolean result;
            if (other == this) {
                result = true;
            } else {
                @SuppressWarnings("unchecked") // don't care about the generics here; important is that we can get at the referent
                WeakReferenceThatIsEqualToItselfAndItsReferent otherAsRef = (WeakReferenceThatIsEqualToItselfAndItsReferent) other;
                K referent = get();
                K otherReferent = otherAsRef.get();
                result = Util.equalsWithNull(referent, otherReferent);
            }
            return result;
        }
    }
    
    private class KeySet extends AbstractSet<K> {
        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return map.containsKey(o);
        }
        
        @Override
        public boolean remove(Object o) {
            return map.remove(o) != null;
        }

        @Override
        public Iterator<K> iterator() {
            purgeStaleEntries();
            final Iterator<Map.Entry<WeakReferenceThatIsEqualToItselfAndItsReferent, V>> iterator = map.entrySet().iterator();
            return new Iterator<K>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public K next() {
                    return iterator.next().getKey().get();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }
    }
    
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            purgeStaleEntries();
            final Iterator<Map.Entry<WeakReferenceThatIsEqualToItselfAndItsReferent, V>> iterator = map.entrySet().iterator();
            return new Iterator<Map.Entry<K, V>>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Map.Entry<K, V> next() {
                    final Map.Entry<WeakReferenceThatIsEqualToItselfAndItsReferent, V> e = iterator.next();
                    return new Map.Entry<K, V>() {
                        @Override
                        public K getKey() {
                            return e.getKey().get();
                        }

                        @Override
                        public V getValue() {
                            return e.getValue();
                        }

                        @Override
                        public V setValue(V value) {
                            return e.setValue(value);
                        }
                    };
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }
    }
    
    public ConcurrentWeakHashMap() {
        map = new ConcurrentHashMap<WeakReferenceThatIsEqualToItselfAndItsReferent, V>();
        queue = new ReferenceQueue<K>();
    }
    
    private void purgeStaleEntries() {
        Reference<? extends K> ref;
        while ((ref=queue.poll()) != null) {
            map.remove(ref);
        }
    }

    @Override
    public int size() {
        purgeStaleEntries();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        purgeStaleEntries();
        return map.isEmpty();
    }

    @SuppressWarnings("unchecked") // if the cast doesn't work then neither will the equals call, so false will result which is ok
    @Override
    public boolean containsKey(Object key) {
        purgeStaleEntries();
        return map.containsKey(new WeakReferenceThatIsEqualToItselfAndItsReferent((K) key, /* queue */ null));
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @SuppressWarnings("unchecked") // if the cast doesn't work then neither will the equals call, so null will result which is ok
    @Override
    public V get(Object key) {
        purgeStaleEntries();
        return map.get(new WeakReferenceThatIsEqualToItselfAndItsReferent((K) key, /* queue */ null));
    }

    @Override
    public V put(K key, V value) {
        return map.put(new WeakReferenceThatIsEqualToItselfAndItsReferent(key, queue), value);
    }

    @SuppressWarnings("unchecked") // if the cast doesn't work then neither will the equals call, so null will result which is ok
    @Override
    public V remove(Object key) {
        return map.remove(new WeakReferenceThatIsEqualToItselfAndItsReferent((K) key, /* queue */ null));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }

}
