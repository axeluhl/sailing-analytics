package com.sap.sailing.domain.tracking;


public interface RaceChangeListener<ItemType> extends WindListener {
    void gpsFixReceived(GPSFix fix, ItemType competitor);

    void markPassingReceived(MarkPassing markPassing);

    void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);

}
