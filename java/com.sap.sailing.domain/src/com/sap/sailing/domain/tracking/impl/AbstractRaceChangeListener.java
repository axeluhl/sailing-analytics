package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.Wind;

public abstract class AbstractRaceChangeListener implements RaceChangeListener {

    @Override
    public void buoyPositionChanged(GPSFix fix, Buoy buoy) {
    }

    @Override
    public void windDataReceived(Wind wind, WindSource windSource) {
    }

    @Override
    public void windDataRemoved(Wind wind, WindSource windSource) {
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor item) {
    }

    @Override
    public void markPassingReceived(MarkPassing oldMarkPassing, MarkPassing markPassing) {
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
    }

}
