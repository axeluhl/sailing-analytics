package com.sap.sailing.domain.common.racelog;

/**
 * Generally, any {@code AbstractLogEvent} has an author of type {@code AbstractLogEventAuthor} which has a numerical
 * non-negative priority. Lesser numerical values represent higher priority/precedence. This enumeration captures
 * the default priorities the Race Manager App is expected to handle.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public enum AuthorPriority {
    ADMIN(0),
    OFFICER_ON_VESSEL(1),
    SHORE_CONTROL(2),
    DEMO_MODE(3);
    
    private final int priority;

    private AuthorPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
