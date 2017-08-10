package com.sap.sailing.datamining.shared;

import com.sap.sailing.domain.common.NauticalSide;

public class ManeuverSpeedDetailsSettingsImpl extends ManeuverSpeedDetailsSettings {
    
    
    private static final long serialVersionUID = 61329258712144L;
    
    private NauticalSide normalizeManeuverDirection;
    
    public static ManeuverSpeedDetailsSettings createDefault() {
        return new ManeuverSpeedDetailsSettingsImpl(null);
    }
    
    public ManeuverSpeedDetailsSettingsImpl() {
    }

    public ManeuverSpeedDetailsSettingsImpl(NauticalSide normalizeManeuverDirection) {
        this.normalizeManeuverDirection = normalizeManeuverDirection;
    }

    @Override
    public boolean isNormalizeManeuverDirection() {
        return normalizeManeuverDirection != null;
    }

    @Override
    public NauticalSide getNormalizedManeuverDirection() {
        return normalizeManeuverDirection;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((normalizeManeuverDirection == null) ? 0 : normalizeManeuverDirection.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManeuverSpeedDetailsSettingsImpl other = (ManeuverSpeedDetailsSettingsImpl) obj;
        if (normalizeManeuverDirection != other.normalizeManeuverDirection)
            return false;
        return true;
    }
    

}
