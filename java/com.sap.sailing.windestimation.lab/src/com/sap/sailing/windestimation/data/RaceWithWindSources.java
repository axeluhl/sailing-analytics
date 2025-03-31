package com.sap.sailing.windestimation.data;

import java.util.List;

public class RaceWithWindSources {

    private final String regattaName;
    private final String raceName;
    private final WindSourceMetadata windSourceMetadata;
    private final List<WindSourceWithFixes> windSources;

    public RaceWithWindSources(String regattaName, String raceName, WindSourceMetadata windSourceMetadata,
            List<WindSourceWithFixes> windSources) {
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.windSourceMetadata = windSourceMetadata;
        this.windSources = windSources;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public String getRaceName() {
        return raceName;
    }

    public WindSourceMetadata getWindSourceMetadata() {
        return windSourceMetadata;
    }

    public List<WindSourceWithFixes> getWindSources() {
        return windSources;
    }

}
