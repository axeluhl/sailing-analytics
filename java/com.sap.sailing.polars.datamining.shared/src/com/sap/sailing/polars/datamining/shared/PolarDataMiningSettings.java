package com.sap.sailing.polars.datamining.shared;

import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sse.common.settings.SerializableSettings;

/**
 * Settings for polar datamining per datamining UI. Allows the user to configure some settings that are applied during
 * retrieval and aggregation.
 * 
 * @author D054528 (Frederik Petersen)
 *
 */
public abstract class PolarDataMiningSettings extends SerializableSettings {

    private static final long serialVersionUID = 3670315004615987482L;

    public abstract Integer getMinimumDataCountPerGraph();

    public abstract double getMinimumWindConfidence();
    
    public abstract boolean applyMinimumWindConfidence();

    public abstract Integer getMinimumDataCountPerAngle();

    public abstract int getNumberOfHistogramColumns();

    public abstract boolean useOnlyWindGaugesForWindSpeed();
    
    public abstract boolean useOnlyEstimatedForWindDirection();

    public abstract WindSpeedSteppingWithMaxDistance getWindSpeedStepping();

    public abstract boolean areDefault();

}
