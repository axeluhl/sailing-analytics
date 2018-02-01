package com.sap.sailing.domain.common;

/**
 * Thrown when wind information is missing where it's desperately needed. This can,
 * e.g., be the case if no wind information was measured or manually entered or
 * derived from, e.g., tacking angles.
 *  
 * @author Axel Uhl (d043530)
 *
 */
public class NoWindException extends Exception {
    private static final long serialVersionUID = 4595827888350748956L;
    
    @SuppressWarnings("unused") // required for some serialization frameworks such as GWT RPC
    private NoWindException() {}
    
    public NoWindException(String message) {
        super(message);
    }
}
