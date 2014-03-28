package com.sap.sse.common;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {
	private static final long serialVersionUID = 814998330023824400L;

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
