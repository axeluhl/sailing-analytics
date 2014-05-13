package com.sap.sse.common;

import java.io.Serializable;

public class Triple<A, B, C> implements Serializable {
	private static final long serialVersionUID = 6906848859104077300L;

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
