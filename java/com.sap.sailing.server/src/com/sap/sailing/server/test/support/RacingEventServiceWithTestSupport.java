package com.sap.sailing.server.test.support;

import com.sap.sailing.server.RacingEventService;

public interface RacingEventServiceWithTestSupport extends RacingEventService {
    /**
     * Wipes out the complete state. This method is only used or testing.
     * 
     * Note: Simple properties like the time delay to the 'live' timepoint are not reseted.
     * 
     * @throws Exception
     */
    void clearState() throws Exception;
}
