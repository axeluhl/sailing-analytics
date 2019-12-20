package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.DeviceIdentifier;

/**
 * A specification how a mark's position shall be obtained. Typical cases can be a fixed position,
 * or a {@link DeviceIdentifier} telling which device to use to track the object.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Positioning {
    <T> T accept(PositioningVisitor<T> visitor);
}
