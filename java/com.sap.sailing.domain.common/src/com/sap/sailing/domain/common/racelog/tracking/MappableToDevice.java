package com.sap.sailing.domain.common.racelog.tracking;

/**
 * Marker interface that can be applied to DTOs that represent domain objects
 * that can be mapped to a device for racelog-tracking (see {@link DeviceMapping}.
 * By doing so, they can be referred without having to use {@link Serializable}
 * as type - which has a considerable performance impact for GWT.
 * @author Fredrik Teschke
 *
 */
public interface MappableToDevice {
    String getIdAsString();
}
