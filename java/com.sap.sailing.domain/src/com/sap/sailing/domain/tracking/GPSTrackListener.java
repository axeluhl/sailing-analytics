package com.sap.sailing.domain.tracking;

public interface GPSTrackListener<ItemType> {
    void gpsFixReceived(GPSFix fix, ItemType item);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);
}
