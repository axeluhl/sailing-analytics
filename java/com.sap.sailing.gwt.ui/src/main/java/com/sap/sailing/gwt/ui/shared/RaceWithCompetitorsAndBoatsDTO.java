package com.sap.sailing.gwt.ui.shared;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;

public class RaceWithCompetitorsAndBoatsDTO extends RaceDTO {
    private static final long serialVersionUID = 5389509649915599951L;
    private Map<CompetitorWithBoatDTO, BoatDTO> competitorsAndBoats;

    /**
     * Constructor for GWT serialization.
     */
    RaceWithCompetitorsAndBoatsDTO() {}

    public RaceWithCompetitorsAndBoatsDTO(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorWithBoatDTO, BoatDTO> competitorsAndBoats,
            TrackedRaceDTO trackedRace, boolean isCurrentlyTracked) {
        super(raceIdentifier, trackedRace, isCurrentlyTracked);
        this.competitorsAndBoats = competitorsAndBoats;
    }

    public RaceWithCompetitorsAndBoatsDTO(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorWithBoatDTO, BoatDTO> competitorsAndBoats) {
        super(raceIdentifier);
        this.competitorsAndBoats = competitorsAndBoats;
    }

    public Map<CompetitorWithBoatDTO, BoatDTO> getCompetitorsAndBoats() {
        return competitorsAndBoats;
    }

    public Iterable<CompetitorWithBoatDTO> getCompetitors() {
        return competitorsAndBoats.keySet();
    }
    
    public Collection<BoatDTO> getBoats() {
        return competitorsAndBoats.values();
    }

}
