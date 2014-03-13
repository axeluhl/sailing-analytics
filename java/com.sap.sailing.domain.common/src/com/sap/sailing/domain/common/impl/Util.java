package com.sap.sailing.domain.common.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Util {
    /**
     * Adds all elements from <code>what</code> to <code>addTo</code> and returns <code>addTo</code> for chained use.
     */
    public static <T> Collection<T> addAll(Iterable<? extends T> what, Collection<T> addTo) {
        for (T t : what) {
            addTo.add(t);
        }
        return addTo;
    }
    
    public static <T> void removeAll(Iterable<T> what, Collection<T> removeFrom) {
        for (T t : what) {
            removeFrom.remove(t);
        }
    }

    public static <T> T[] toArray(Iterable<T> what, T[] arr) {
        List<T> list = new ArrayList<T>();
        addAll(what, list);
        return list.toArray(arr);
    }
    
    public static <T> int size(Iterable<T> i) {
        if (i instanceof Collection<?>) {
            return ((Collection<?>) i).size();
        } else {
            int result = 0;
            Iterator<T> iter = i.iterator();
            while (iter.hasNext()) {
                result++;
                iter.next();
            }
            return result;
        }
    }
    
    public static <T> int indexOf(Iterable<? extends T> i, T t) {
        int result;
        if (i instanceof List<?>) {
            List<?> list = (List<?>) i;
            result = list.indexOf(t);
        } else {
            boolean found = false;
            int counter = 0;
            for (T it : i) {
                if (it == null && t == null
                        || it != null && it.equals(t)) {
                    result = counter;
                    found = true;
                    break;
                }
                counter++;
            }
            if (found) {
                result = counter;
            } else {
                result = -1;
            }
        }
        return result;
    }
    
    public static <T> boolean equals(Iterable<? extends T> a, Iterable<? extends T> b) {
        Iterator<? extends T> aIter = a.iterator();
        Iterator<? extends T> bIter = b.iterator();
        while (aIter.hasNext() && bIter.hasNext()) {
            T ao = aIter.next();
            T bo = bIter.next();
            if (!ao.equals(bo)) {
                return false;
            }
        }
        return !aIter.hasNext() && !bIter.hasNext();
    }
    
    public static <T> T get(Iterable<T> iterable, int i) {
        if (iterable instanceof List<?>) {
            List<T> l = (List<T>) iterable;
            return l.get(i);
        } else {
            Iterator<T> iter = iterable.iterator();
            T result = iter.next();
            for (int j=0; j<i; j++) {
                result = iter.next();
            }
            return result;
        }
    }

    public static <T> boolean contains(Iterable<T> ts, T t) {
        if (ts instanceof Collection<?>) {
            return ((Collection<?>) ts).contains(t);
        } else {
            for (T t2 : ts) {
                if (t2.equals(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static <T> boolean isEmpty(Iterable<T> ts) {
        if (ts instanceof Collection<?>) {
            return ((Collection<?>) ts).isEmpty();
        } else {
            return !ts.iterator().hasNext();
        }
    }
    
    public static class Pair<A, B> implements Serializable {
        private static final long serialVersionUID = -7631774746419135931L;

        private A a;

        private B b;

        private transient int hashCode;

        @SuppressWarnings("unused") // required for some serialization frameworks such as GWT RPC
        private Pair() {}
        
        public Pair( A a, B b ) {
            this.a = a;
            this.b = b;
            hashCode = 0;
        }

        public A getA( ) {
            return a;
        }

        public B getB( ) {
            return b;
        }

        @Override
        public int hashCode( ) {
            if ( hashCode == 0 ) {
                hashCode = 17;
                hashCode = 37 * hashCode + ( a != null ? a.hashCode( ) : 0 );
                hashCode = 37 * hashCode + ( b != null ? b.hashCode( ) : 0 );
            }
            return hashCode;
        }

        @Override
        public boolean equals( Object obj ) {
            boolean result;
            if ( this == obj ) {
                result = true;
            } else if ( obj instanceof Pair<?, ?> ) {
                Pair<?, ?> pair = (Pair<?, ?>) obj;
                result = ( this.a != null && this.a.equals( pair.a ) || this.a == null && pair.a == null ) && ( this.b != null && this.b.equals( pair.b ) || this.b == null && pair.b == null );
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public String toString( ) {
            return "[" + (a==null?"null":a.toString( )) + ", " +
                (b==null?"null":b.toString( )) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    public static class Triple<A, B, C> implements Serializable {
        private static final long serialVersionUID = 6806146864367514601L;

        private A a;

        private B b;

        private C c;

        private transient int hashCode;

        @SuppressWarnings("unused") // required for some serialization frameworks such as GWT RPC
        private Triple() {}

        public Triple( A a, B b, C c ) {
            this.a = a;
            this.b = b;
            this.c = c;
            hashCode = 0;
        }

        public A getA( ) {
            return a;
        }

        public B getB( ) {
            return b;
        }

        public C getC( ) {
            return c;
        }

        @Override
        public int hashCode( ) {
            if ( hashCode == 0 ) {
                hashCode = 17;
                hashCode = 37 * hashCode + ( a != null ? a.hashCode( ) : 0 );
                hashCode = 37 * hashCode + ( b != null ? b.hashCode( ) : 0 );
                hashCode = 37 * hashCode + ( c != null ? c.hashCode( ) : 0 );
            }
            return hashCode;
        }

        @Override
        public boolean equals( Object obj ) {
            boolean result;
            if ( this == obj ) {
                result = true;
            } else if ( obj instanceof Triple<?, ?, ?> ) {
                Triple<?, ?, ?> thrice = (Triple<?, ?, ?>) obj;
                result = ( this.a != null && this.a.equals( thrice.a ) || this.a == null && thrice.a == null ) && ( this.b != null && this.b.equals( thrice.b ) || this.b == null && thrice.b == null ) && ( this.c != null && this.c.equals( thrice.c ) || this.c == null && thrice.c == null );
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public String toString( ) {
            return "[" + a + ", " + b + ", " + c + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
    }
    

    public static boolean equalsWithNull(Object o1, Object o2) {
        final boolean result;
        if (o1 == null) {
            result = (o2 == null);
        } else {
            if (o2 == null) {
                result = false;
            } else {
                result = o1.equals(o2);
            }
        }
        return result;
    }
    
    public static <T> int compareToWithNull(Comparable<T> o1, T o2) {
        final int result;
        if (o1 == null) {
            if (o2 == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else {
            if (o2 == null) {
                result = 1;
            } else {
                result = o1.compareTo(o2);
            }
        }
        return result;
    }
    
}
