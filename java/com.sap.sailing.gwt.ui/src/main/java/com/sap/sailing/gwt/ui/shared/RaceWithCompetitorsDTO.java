package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;

public class RaceWithCompetitorsDTO extends RaceDTO {
    private static final long serialVersionUID = 5389509649915599951L;
    private Iterable<CompetitorDTO> competitors;

    /**
     * Constructor for GWT serialization.
     */
    RaceWithCompetitorsDTO() {}

    public RaceWithCompetitorsDTO(RegattaAndRaceIdentifier raceIdentifier, Iterable<CompetitorDTO> competitors,
            TrackedRaceDTO trackedRace, boolean isCurrentlyTracked) {
        super(raceIdentifier, trackedRace, isCurrentlyTracked);
        this.competitors = competitors;
    }

    public RaceWithCompetitorsDTO(RegattaAndRaceIdentifier raceIdentifier, Iterable<CompetitorDTO> competitors) {
        super(raceIdentifier);
        this.competitors = competitors;
    }

    public Iterable<CompetitorDTO> getCompetitors() {
        return competitors;
    }
}
