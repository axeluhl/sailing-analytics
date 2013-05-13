package com.sap.sailing.domain.common.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
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
    
    /**
     * Natural Order String Comparison inspired by the idea of Martin Pool and Dave Koelle.
     * 
     * Natural order compares mixed (alphanumeric) string in a more intuitive way:
     *          a < a0 < a1 < a1a < a1b < a2 < a10 < a20
     *
     */
    public static class NaturalComparator implements Comparator<String> {

        /**
         * Compare the passed strings in natural order. 
         */
        @Override
        public int compare(String a, String b) {
            int result = 0;
            int aIndex = 0;
            int bIndex = 0;
            int aLength = a.length();
            int bLength = b.length();
            
            while(aIndex < aLength && bIndex < bLength) {
                // Get next block of all-char or all-digit substring
                String aBlock = getBlock(a, aIndex);
                aIndex += aBlock.length();
                String bBlock = getBlock(b, bIndex);
                bIndex += bBlock.length();
                
                // Compare all-digit blocks as numbers - all-char blocks as strings
                if (Character.isDigit(aBlock.charAt(0)) && Character.isDigit(bBlock.charAt(0))) {
                    result = Integer.valueOf(aBlock).compareTo(Integer.valueOf(bBlock));
                } else {
                    result = aBlock.compareTo(bBlock);
                }
                
                if (result != 0) {
                    return result;
                }
            }
            
            // Strings may be of different size
            return aLength - bLength;
        }

        private String getBlock(String value, int index) {
            int valueLength = value.length();
            StringBuilder blockBuilder = new StringBuilder();
            
            char ch = value.charAt(index++);
            blockBuilder.append(ch);
            
            if (Character.isDigit(ch)) {
                while (index < valueLength) {
                    ch = value.charAt(index++);
                    if (!Character.isDigit(ch)) {
                        break;
                    } else {
                        blockBuilder.append(ch);
                    }
                }
            } else {
                while (index < valueLength) {
                    ch = value.charAt(index++);
                    if (Character.isDigit(ch)) {
                        break;
                    } else {
                        blockBuilder.append(ch);
                    }
                }
            }
            
            return blockBuilder.toString();
        }
        
    }

}
