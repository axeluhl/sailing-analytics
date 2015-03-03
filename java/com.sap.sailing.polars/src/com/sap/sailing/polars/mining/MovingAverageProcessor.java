package com.sap.sailing.polars.mining;

import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;

public interface MovingAverageProcessor extends Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, Void>{
    SpeedWithBearingWithConfidence<Void> getAverageSpeedAndCourseOverGround(BoatClass boatClass,
			Speed windSpeed, LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException;

    Set<BoatClass> getAvailableBoatClasses();
}