package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Triple;

/**
 * Used on a {@link MarkConfiguration} as a response to a client, providing a result-oriented view on a mark
 * configuration. In particular, positioning data may be contained that informs clients about the current tracking and
 * positioning state of the mark in question.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkConfigurationResponseAnnotation {
    GPSFix getLastKnownPosition();
    
    /**
     * @return a non-{@code null} but possibly empty sequence of device mappings; the triple holds the
     * device identifier, the time range during which the device was assigned, and optionally the last
     * known GPS fix if one has been received.
     */
    Iterable<Triple<DeviceIdentifier, TimeRange, GPSFix>> getDeviceMappings();
}
