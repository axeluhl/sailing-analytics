package com.sap.sailing.polars.mining;

import java.io.Serializable;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;

public interface MovingAverageProcessor extends Processor<GroupedDataEntry<GPSFixMovingWithPolarContext>, Void>,
        Serializable {
    SpeedWithBearingWithConfidence<Void> getAverageSpeedAndCourseOverGround(BoatClass boatClass, Speed windSpeed,
            LegType legType) throws NotEnoughDataHasBeenAddedException;

    ClusterGroup<Speed> getSpeedCluster();
}