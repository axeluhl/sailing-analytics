package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class RaceWithCompetitorsDTO extends RaceDTO {
    public Iterable<CompetitorDTO> competitors;

    /**
     * Constructor for GWT serialization.
     */
    RaceWithCompetitorsDTO() {}

    public RaceWithCompetitorsDTO(RegattaAndRaceIdentifier raceIdentifier, Iterable<CompetitorDTO> competitors, TrackedRaceDTO trackedRace) {
        super(raceIdentifier, trackedRace);
        this.competitors = competitors;
    }

    public RaceWithCompetitorsDTO(RegattaAndRaceIdentifier raceIdentifier, Iterable<CompetitorDTO> competitors) {
        super(raceIdentifier);
        this.competitors = competitors;
    }
}
