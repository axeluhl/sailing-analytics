package com.sap.sailing.domain.tracking;

import java.io.Serializable;

public interface GPSTrackListener<ItemType> extends Serializable {
    void gpsFixReceived(GPSFix fix, ItemType item);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);
}
