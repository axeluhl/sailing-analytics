package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;

public class RaceWithCompetitorsDTO extends RaceDTO {
    private static final long serialVersionUID = 5389509649915599951L;
    private List<CompetitorDTO> competitors;

    /**
     * Constructor for GWT serialization.
     */
    RaceWithCompetitorsDTO() {}

    public RaceWithCompetitorsDTO(RegattaAndRaceIdentifier raceIdentifier, List<CompetitorDTO> competitors,
            TrackedRaceDTO trackedRace, boolean isCurrentlyTracked) {
        super(raceIdentifier, trackedRace, isCurrentlyTracked);
        this.competitors = competitors;
    }

    public RaceWithCompetitorsDTO(RegattaAndRaceIdentifier raceIdentifier, List<CompetitorDTO> competitors) {
        super(raceIdentifier);
        this.competitors = competitors;
    }

    public List<CompetitorDTO> getCompetitors() {
        return competitors;
    }
}
