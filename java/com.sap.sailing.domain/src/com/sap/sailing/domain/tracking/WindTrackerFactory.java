package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sse.security.SecurityService;

/**
 * Constructs wind trackers that link some wind data receiving facility to a {@link TrackedRace}.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface WindTrackerFactory {
    /**
     * @param optionalSecurityService
     *            If available, the {@link SecurityService} can be used for additional security checks like filtering
     *            for data sources/accounts based on the current security context.
     */
    WindTracker createWindTracker(DynamicTrackedRegatta trackedRegatta, RaceDefinition race, boolean correctByDeclination,
            SecurityService optionalSecurityService)
            throws Exception;

    /**
     * Returns a {@link WindTracker} is one has been previously created for <code>race</code> and hasn't been
     * stopped yet, <code>null</code> otherwise.
     */
    WindTracker getExistingWindTracker(RaceDefinition race);
}
