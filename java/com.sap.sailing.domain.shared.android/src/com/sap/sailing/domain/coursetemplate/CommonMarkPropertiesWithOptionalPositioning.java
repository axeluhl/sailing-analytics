package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.Position;

/**
 * Stores properties that can be applied to a mark in the context of an event or a regatta, including the mark's own
 * attributes as well as tracking-related information such as a reference to the tracking device used to track the mark,
 * or a fixed mark position, such as for a land mark, an official lateral or cardinal buoy, a regatta mark in a fixed
 * position or a lighthouse.
 * <p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface CommonMarkPropertiesWithOptionalPositioning extends CommonMarkProperties {
    /**
     * If not {@code null} then a device identifier that can be used to create a device mapping
     * in the scope of a regatta such that the tracking device with the ID returned will be used
     * to track the mark to which these properties are applied. No timing for any device mapping is
     * provided here. It is up to the process of creating and configuring the regatta marks to decide
     * about device mapping time intervals.
     */
    DeviceIdentifier getTrackingDeviceIdentifier();
    
    /**
     * Returns a fixed position to be used to "ping" the mark to which these properties are applied; or {@code null} in
     * case the mark is not at a fixed position or no position is known. In particular, it is considered an error to
     * provide a non-{@code null} fixed position when a non-{@code null} {@link #getTrackingDeviceIdentifier() tracking
     * device identifier} has been provided for these mark properties.
     */
    Position getFixedPosition();
}
