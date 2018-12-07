package com.sap.sailing.windestimation.data;

import java.util.List;

public class RaceWithWindSources {
    
    private final WindSourceMetadata windSourceMetadata;
    private final List<WindSourceWithFixes> windSources;

    public RaceWithWindSources(WindSourceMetadata windSourceMetadata, List<WindSourceWithFixes> windSources) {
        this.windSourceMetadata = windSourceMetadata;
        this.windSources = windSources;
    }

    public WindSourceMetadata getWindSourceMetadata() {
        return windSourceMetadata;
    }

    public List<WindSourceWithFixes> getWindSources() {
        return windSources;
    }
    
}
