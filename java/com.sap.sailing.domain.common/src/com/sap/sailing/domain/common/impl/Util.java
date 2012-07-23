package com.sap.sailing.domain.common.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

public class Util {
    private static final int NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK = 5;
    private static final Logger logger = Logger.getLogger(Util.class.getName());
    
    /**
     * Bug <a href="http://bugs.sun.com/view_bug.do?bug_id=6822370">http://bugs.sun.com/view_bug.do?bug_id=6822370</a> seems
     * dangerous, particularly if it happens in a <code>LiveLeaderboardUpdater</code> thread. Even though the bug is reported to
     * have been fixed in JDK 7(b79) we should be careful. This method tries to acquire a lock, allowing for five seconds to pass.
     * After five seconds and not having retrieved the lock, tries again until the lock has been acquired.
     * @throws InterruptedException 
     */
    public static void lock(Lock lock) {
        boolean locked = false;
        boolean interrupted = false;
        while (!locked) {
            try {
                locked = lock.tryLock(NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK, TimeUnit.SECONDS);
                if (!locked) {
                    logger.info("Couldn't acquire lock in "+NUMBER_OF_SECONDS_TO_WAIT_FOR_LOCK+"s. Trying again...");
                }
            }
            catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        if (interrupted) {
            // re-assert interrupt state that occurred while we
            // were acquiring the lock
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Adds all elements from <code>what</code> to <code>addTo</code> and returns <code>addTo</code> for chained use.
     */
    public static <T> Collection<T> addAll(Iterable<T> what, Collection<T> addTo) {
        for (T t : what) {
            addTo.add(t);
        }
        return addTo;
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
    
    public static <T> int indexO(Iterable<? extends T> i, T t) {
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
}
