package com.sap.sailing.gwt.ui.shared;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;

public class RaceWithCompetitorsAndBoatsDTO extends RaceDTO {
    private static final long serialVersionUID = 5389509649915599951L;
    private Map<CompetitorDTO, BoatDTO> competitorsAndBoats;

    /**
     * Constructor for GWT serialization.
     */
    RaceWithCompetitorsAndBoatsDTO() {}

    public RaceWithCompetitorsAndBoatsDTO(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, BoatDTO> competitorsAndBoats,
            TrackedRaceDTO trackedRace, boolean isCurrentlyTracked) {
        super(raceIdentifier, trackedRace, isCurrentlyTracked);
        this.competitorsAndBoats = competitorsAndBoats;
    }

    public RaceWithCompetitorsAndBoatsDTO(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, BoatDTO> competitorsAndBoats) {
        super(raceIdentifier);
        this.competitorsAndBoats = competitorsAndBoats;
    }

    public Map<CompetitorDTO, BoatDTO> getCompetitorsAndBoats() {
        return competitorsAndBoats;
    }

    public Iterable<CompetitorDTO> getCompetitors() {
        return competitorsAndBoats.keySet();
    }
    
    public Collection<BoatDTO> getBoats() {
        return competitorsAndBoats.values();
    }

}
