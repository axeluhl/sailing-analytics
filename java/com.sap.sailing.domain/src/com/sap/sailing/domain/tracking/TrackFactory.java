package com.sap.sailing.domain.tracking;

public interface TrackFactory<TrackT extends DynamicTrack<?>> {
    
    TrackT get();

}
