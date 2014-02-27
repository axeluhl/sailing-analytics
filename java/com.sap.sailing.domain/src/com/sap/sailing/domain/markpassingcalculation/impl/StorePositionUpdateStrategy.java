package com.sap.sailing.domain.markpassingcalculation.impl;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.tracking.GPSFix;

public interface StorePositionUpdateStrategy {
    void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes);
}
