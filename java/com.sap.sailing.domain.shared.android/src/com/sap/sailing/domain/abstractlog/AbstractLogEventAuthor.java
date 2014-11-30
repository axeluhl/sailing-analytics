package com.sap.sailing.domain.abstractlog;

import com.sap.sse.common.Named;

/**
 * Multiple devices may concurrently post to a single {@link AbstractLog}. In this case it is important to understand
 * who posted what. The "who" is described by instances of this class. In particular, an author has a name and a
 * priority which is then used to arbitrate between different posts on the same subject, such as a race's start time
 * or transitions from one race phase to another.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface AbstractLogEventAuthor extends Named, Comparable<AbstractLogEventAuthor> {
    
    public static final int PRIORITY_COMPATIBILITY = 16;
    public static final String NAME_COMPATIBILITY = "Compatibility";
    
    /**
     * A lesser number represents a higher priority. Suggested priority levels:
     * <ol>
     * <li>Start Vessel</li>
     * <li>Finish Vessel</li>
     * <li>Shore Control</li>
     * <li>Compatibility priority for old events</li>
     * </ol>
     */
    int getPriority();
}
