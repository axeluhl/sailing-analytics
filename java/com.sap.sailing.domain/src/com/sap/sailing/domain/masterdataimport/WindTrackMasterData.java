package com.sap.sailing.domain.masterdataimport;

import java.io.Serializable;
import java.util.Set;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.tracking.Wind;

public class WindTrackMasterData {

    private final WindSourceType windSourceType;
    private final Serializable windSourceId;
    private final Set<Wind> fixes;
    private final String regattaName;
    private final String raceName;
    private final Serializable raceId;

    public WindTrackMasterData(String windSourceTypeName, Serializable windSourceId, Set<Wind> fixes,
            String regattaName, String raceName, Serializable raceId) {
        this.windSourceId = windSourceId;
        this.fixes = fixes;
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.raceId = raceId;
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
    
    public String getRegattaName() {
        return regattaName;
    }

    public String getRaceName() {
        return raceName;
    }

    public Serializable getRaceId() {
        return raceId;
    }

    
    public WindSource getWindSource() {
        WindSource source;
        if (windSourceId != null) {
            source = new WindSourceWithAdditionalID(windSourceType, windSourceId.toString());
        } else {
            source = new WindSourceImpl(windSourceType);
        }
        return source;
    }

}
