package com.sap.sailing.datamining.shared;

import com.sap.sailing.domain.common.NauticalSide;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpeedDetailsSettingsImpl extends ManeuverSpeedDetailsSettings {

    private static final long serialVersionUID = 61329258712144L;

    private NauticalSide normalizeManeuverDirection;

    private boolean maneuverDirectionEqualWeighingEnabled;

    public static ManeuverSpeedDetailsSettings createDefault() {
        return new ManeuverSpeedDetailsSettingsImpl(null, false);
    }

    public ManeuverSpeedDetailsSettingsImpl() {
    }

    public ManeuverSpeedDetailsSettingsImpl(NauticalSide normalizeManeuverDirection,
            boolean maneuverDirectionEqualWeighingEnabled) {
        this.normalizeManeuverDirection = normalizeManeuverDirection;
        this.maneuverDirectionEqualWeighingEnabled = maneuverDirectionEqualWeighingEnabled;
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
    public boolean isManeuverDirectionEqualWeightingEnabled() {
        return maneuverDirectionEqualWeighingEnabled;
    }

}
