package com.sap.sailing.windestimation.data;

import java.util.List;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;

public class WindSourceWithFixes {

    private final String dbId;
    private final WindSourceType windSourceType;
    private final WindSourceMetadata windSourceMetadata;
    private final List<Wind> windFixes;

    public WindSourceWithFixes(String dbId, WindSourceMetadata windSourceMetadata, WindSourceType windSourceType,
            List<Wind> windFixes) {
        this.dbId = dbId;
        this.windSourceMetadata = windSourceMetadata;
        this.windSourceType = windSourceType;
        this.windFixes = windFixes;
    }

    public WindSourceType getWindSourceType() {
        return windSourceType;
    }

    public WindSourceMetadata getWindSourceMetadata() {
        return windSourceMetadata;
    }

    public List<Wind> getWindFixes() {
        return windFixes;
    }

    public String getDbId() {
        return dbId;
    }

}
