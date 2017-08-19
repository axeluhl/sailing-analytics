package com.sap.sailing.datamining.shared;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.impl.CentralAngleDistance;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sse.datamining.shared.SerializationDummy;

@SuppressWarnings("unused")
public final class SailingDataMiningSerializationDummy implements SerializationDummy {
    private static final long serialVersionUID = 2L;
    
    private LegType legType;
    private Distance distance;
    private Bearing bearing;
    private CentralAngleDistance centralAngleDistance;
    private MeterDistance meterDistance;
    private NauticalMileDistance nauticalMileDistance;
    private ManeuverType maneuverType;
    private Tack tack;
    private NauticalSide nauticalSide;
    private ManeuverSpeedDetailsAggregation maneuverSpeedDetailsAggregation;
    private ManeuverSpeedDetailsAggregationImpl maneuverSpeedDetailsAggregationImpl;
    private ManeuverSpeedDetailsSettings maneuverSpeedDetailsSettings;
    private ManeuverSpeedDetailsSettingsImpl maneuverSpeedDetailsSettingsImpl;
    private ManeuverSettings maneuverSettings;
    private ManeuverSettingsImpl maneuverSettingsImpl;
    
    private SailingDataMiningSerializationDummy() { }
    
}
