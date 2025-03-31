package com.sap.sailing.domain.maneuverdetection;

/**
 * Thrown when there are no fixes for competitor track analysis.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class NoFixesException extends Exception {

    private static final long serialVersionUID = 6600848219757467158L;

    public NoFixesException() {
    }

    public NoFixesException(String message) {
        super(message);
    }

}
