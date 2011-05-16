package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;

public interface RawListener<FixType extends GPSFix> {
    void gpsFixReceived(FixType fix, TrackedRace trackedRace, Competitor competitor);
}
