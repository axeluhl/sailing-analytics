package com.sap.sailing.domain.persistence.racelog.tracking;

import com.sap.sailing.domain.common.racelog.tracking.TransformationHandler;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;

/**
 * The resulting {@link Object} is expected to be either a simple {@link Object} that can serialized,
 * or a structured {@link DBObject}.
 * @author Fredrik Teschke
 *
 */
public interface DeviceIdentifierMongoHandler extends TransformationHandler<DeviceIdentifier, Object> {
}
