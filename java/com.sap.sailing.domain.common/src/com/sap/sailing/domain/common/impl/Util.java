package com.sap.sailing.domain.common.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Util {
    /**
     * Adds all elements from <code>what</code> to <code>addTo</code> and returns <code>addTo</code> for chained use.
     */
    public static <T> Collection<T> addAll(final Iterable<T> what, final Collection<T> addTo) {
        for (final T t : what) {
            addTo.add(t);
        }
        return addTo;
    }

    public static <T> int size(final Iterable<T> i) {
        if (i instanceof Collection<?>) {
            return ((Collection<?>) i).size();
        } else {
            int result = 0;
            final Iterator<T> iter = i.iterator();
            while (iter.hasNext()) {
                result++;
                iter.next();
            }
            return result;
        }
    }

    public static <T> int indexO(final Iterable<? extends T> i, final T t) {
        int result;
        if (i instanceof List<?>) {
            final List<?> list = (List<?>) i;
            result = list.indexOf(t);
        } else {
            boolean found = false;
            int counter = 0;
            for (final T it : i) {
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

    public static <T> boolean equals(final Iterable<? extends T> a, final Iterable<? extends T> b) {
        final Iterator<? extends T> aIter = a.iterator();
        final Iterator<? extends T> bIter = b.iterator();
        while (aIter.hasNext() && bIter.hasNext()) {
            final T ao = aIter.next();
            final T bo = bIter.next();
            if (!ao.equals(bo)) {
                return false;
            }
        }
        return !aIter.hasNext() && !bIter.hasNext();
    }

    public static <T> T get(final Iterable<T> iterable, final int i) {
        if (iterable instanceof List<?>) {
            final List<T> l = (List<T>) iterable;
            return l.get(i);
        } else {
            final Iterator<T> iter = iterable.iterator();
            T result = iter.next();
            for (int j=0; j<i; j++) {
                result = iter.next();
            }
            return result;
        }
    }

    public static <T> boolean contains(final Iterable<T> ts, final T t) {
        if (ts instanceof Collection<?>) {
            return ((Collection<?>) ts).contains(t);
        } else {
            for (final T t2 : ts) {
                if (t2.equals(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static <T> boolean isEmpty(final Iterable<T> ts) {
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

        public Pair( final A a, final B b ) {
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
        public boolean equals( final Object obj ) {
            boolean result;
            if ( this == obj ) {
                result = true;
            } else if ( obj instanceof Pair<?, ?> ) {
                final Pair<?, ?> pair = (Pair<?, ?>) obj;
                result = ( this.a != null && this.a.equals( pair.a ) || this.a == null && pair.a == null ) && ( this.b != null && this.b.equals( pair.b ) || this.b == null && pair.b == null );
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public String toString( ) {
            return "[" + (a==null?"null":a.toString( )) + ", " +
                    (b==null?"null":b.toString( )) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
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

        public Triple( final A a, final B b, final C c ) {
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
        public boolean equals( final Object obj ) {

            boolean result;
            if ( this == obj ) {
                result = true;
            } else if ( obj instanceof Triple<?, ?, ?> ) {
                final Triple<?, ?, ?> thrice = (Triple<?, ?, ?>) obj;
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

    public static class Quadruple<A, B, C, D> implements Serializable {

        private static final long serialVersionUID = 2120209172266608653L;

        private A a;

        private B b;

        private C c;

        private D d;

        private transient int hashCode;

        @SuppressWarnings("unused")
        // required for some serialization frameworks such as GWT RPC
        private Quadruple() {
        }

        public Quadruple(final A a, final B b, final C c, final D d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.hashCode = 0;
        }

        public A getA() {
            return this.a;
        }

        public B getB() {
            return this.b;
        }

        public C getC() {
            return this.c;
        }

        public D getD() {
            return this.d;
        }

        @Override
        public int hashCode() {
            if (this.hashCode == 0) {
                this.hashCode = 17;
                this.hashCode = 37 * this.hashCode + (this.a != null ? a.hashCode() : 0);
                this.hashCode = 37 * this.hashCode + (this.b != null ? b.hashCode() : 0);
                this.hashCode = 37 * this.hashCode + (this.c != null ? c.hashCode() : 0);
                this.hashCode = 37 * this.hashCode + (this.d != null ? d.hashCode() : 0);
            }
            return this.hashCode;
        }

        @Override
        public boolean equals(final Object obj) {

            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Triple<?, ?, ?>) {
                final Quadruple<?, ?, ?, ?> thrice = (Quadruple<?, ?, ?, ?>) obj;
                result = (this.a != null && this.a.equals(thrice.a) || this.a == null && thrice.a == null)
                        && (this.b != null && this.b.equals(thrice.b) || this.b == null && thrice.b == null)
                        && (this.c != null && this.c.equals(thrice.c) || this.c == null && thrice.c == null)
                        && (this.d != null && this.d.equals(thrice.d) || this.d == null && thrice.d == null);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public String toString() {

            return "[" + this.a + ", " + this.b + ", " + this.c + ", " + this.d + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
    }
}
