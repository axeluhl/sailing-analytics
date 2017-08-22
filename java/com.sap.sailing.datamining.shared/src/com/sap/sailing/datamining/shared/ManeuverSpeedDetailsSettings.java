package com.sap.sailing.datamining.shared;

import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sse.common.settings.SerializableSettings;

public abstract class ManeuverSpeedDetailsSettings extends SerializableSettings {

    private static final long serialVersionUID = -39384430331972342L;
    
    public abstract boolean isNormalizeManeuverDirection();
    public abstract NauticalSide getNormalizedManeuverDirection();
    public abstract boolean isManeuverDirectionEqualWeightingEnabled();

}

