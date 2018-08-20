package com.sap.sailing.domain.tracking;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.sap.sse.common.TimePoint;


public interface StartTimeChangedListener {

    void startTimeChanged(TimePoint newTimePoint) throws MalformedURLException, IOException, URISyntaxException;
    
}
