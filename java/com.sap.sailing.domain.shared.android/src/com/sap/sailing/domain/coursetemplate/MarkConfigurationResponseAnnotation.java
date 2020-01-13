package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util.Pair;

/**
 * Used on a {@link MarkConfiguration} as a response to a client, providing a result-oriented view on a mark
 * configuration. In particular, positioning data may be contained that informs clients about the current tracking and
 * positioning state of the mark in question.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkConfigurationResponseAnnotation {
    // TODO decide if it would be beneficial to use GPSFix instead. Most but not all code paths may provide a GPSFix but MarkProperties with a fixed position does not have a TimePoint associated with the Position.
    Position getLastKnownPosition();
    // TODO include the last known position per mapping
    Iterable<Pair<DeviceIdentifier, TimeRange>> getDeviceMappings();
}
