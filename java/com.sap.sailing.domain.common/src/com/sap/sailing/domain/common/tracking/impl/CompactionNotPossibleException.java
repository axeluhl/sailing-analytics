package com.sap.sailing.domain.common.tracking.impl;

/**
 * Throws when an attempt is made to turn a fix of some sort into a more compact form, probably
 * slightly reducing accuracy and/or reducing value ranges, such as for the speeds possible, where
 * the fix presented cannot be turned into its compact form without exceeding a range. This would
 * lead to an overflow or wrap-around and cannot be tolerated. Clients need to handle this exception
 * by skipping compaction and using the original fix instead.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompactionNotPossibleException extends Exception {
    private static final long serialVersionUID = -723044264487667717L;

    public CompactionNotPossibleException() {
        super();
    }

    public CompactionNotPossibleException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CompactionNotPossibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompactionNotPossibleException(String message) {
        super(message);
    }

    public CompactionNotPossibleException(Throwable cause) {
        super(cause);
    }

}
