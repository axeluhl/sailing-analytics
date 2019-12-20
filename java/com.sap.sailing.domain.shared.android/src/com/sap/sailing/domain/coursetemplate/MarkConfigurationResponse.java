package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;

/**
 * Used as a response to a client, providing a result-oriented view on a mark configuration. In particular, positioning
 * data may be contained that informs clients about the current tracking and positioning state of the mark in question.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface MarkConfigurationResponse extends MarkConfiguration<MarkConfigurationResponse> {
    Position getLastKnownPosition();
    DeviceIdentifier getCurrentTrackingDevice();
}
