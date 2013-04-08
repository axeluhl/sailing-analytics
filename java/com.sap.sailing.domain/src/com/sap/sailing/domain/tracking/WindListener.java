package com.sap.sailing.domain.tracking;



public interface WindListener {
    void windDataReceived(Wind wind);
    
    void windDataRemoved(Wind wind);

    void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage);
}
