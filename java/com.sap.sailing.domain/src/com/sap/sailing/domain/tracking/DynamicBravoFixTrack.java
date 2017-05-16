package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sse.common.WithID;

/**
 * Mutable/dynamic version of {@link BravoFixTrack}.
 *
 * @param <ItemType> the type of item this track is mapped to
 */
public interface DynamicBravoFixTrack<ItemType extends WithID & Serializable> extends BravoFixTrack<ItemType>,
        DynamicSensorFixTrack<ItemType, BravoFix> {
}
