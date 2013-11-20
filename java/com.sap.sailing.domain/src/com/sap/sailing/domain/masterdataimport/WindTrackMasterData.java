package com.sap.sailing.domain.masterdataimport;

import java.io.Serializable;
import java.util.Set;

import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.tracking.Wind;

public class WindTrackMasterData {

    private final WindSourceType windSourceType;
    private final Serializable windSourceId;
    private final Set<Wind> fixes;

    public WindTrackMasterData(String windSourceTypeName, Serializable windSourceId, Set<Wind> fixes) {
        this.windSourceId = windSourceId;
        this.fixes = fixes;
        this.windSourceType = WindSourceType.valueOf(windSourceTypeName);
    }

    public WindSourceType getWindSourceType() {
        return windSourceType;
    }

    public Serializable getWindSourceId() {
        return windSourceId;
    }

    public Set<Wind> getFixes() {
        return fixes;
    }

}
