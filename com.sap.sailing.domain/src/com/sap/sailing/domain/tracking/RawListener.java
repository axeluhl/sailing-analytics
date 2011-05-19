package com.sap.sailing.domain.tracking;


public interface RawListener<ItemType, FixType extends GPSFix> {
    void gpsFixReceived(FixType fix, ItemType itemThatMoved);
}
