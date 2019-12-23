package com.sap.sailing.domain.coursetemplate;

public interface PositioningVisitor<T> {
    T visit(FixedPositioning fixedPositioning);
    
    T visit(TrackingDeviceBasedPositioning trackingDeviceBasedPositioning);
    
    T visit(TrackingDeviceBasedPositioningWithLastKnownPosition trackingDeviceBasedPositioningWithLastKnownPosition);
}
