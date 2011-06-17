package com.sap.sailing.domain.tracking;


public interface RaceChangeListener<ItemType> extends WindListener {
    void gpsFixReceived(GPSFix fix, ItemType competitor);

    void markPassingReceived(MarkPassing markPassing);

    void averagingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);

}
