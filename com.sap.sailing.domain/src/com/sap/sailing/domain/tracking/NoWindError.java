package com.sap.sailing.domain.tracking;

/**
 * The runtime exception variant of {@link NoWindException}. Can be used where no
 * checked exception may be thrown, such as in a comparator's compare method.
 * 
 * @author Axel Uhl
 *
 */
public class NoWindError extends RuntimeException {
    private static final long serialVersionUID = -6279617486075805721L;

    public NoWindError(NoWindException noWindException) {
        super(noWindException);
    }
    
    public NoWindException getCause() {
        return (NoWindException) super.getCause();
    }
}
