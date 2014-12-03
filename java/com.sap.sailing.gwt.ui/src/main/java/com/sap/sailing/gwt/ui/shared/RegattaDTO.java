package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.NamedDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sse.common.Util.Pair;

public class RegattaDTO extends NamedDTO {
    private static final long serialVersionUID = -4594784946348402759L;
    /**
     * May be <code>null</code> in case the boat class is not known
     */
    public BoatClassDTO boatClass;
    public Date startDate;
    public Date endDate;
    public List<RaceWithCompetitorsDTO> races;
    public List<SeriesDTO> series;
    public ScoringSchemeType scoringScheme;
    public UUID defaultCourseAreaUuid;
    public String defaultCourseAreaName;
    public DeviceConfigurationDTO.RegattaConfigurationDTO configuration;
    public boolean useStartTimeInference = true;
    public EventDTO event;

    public RegattaDTO() {}

    public RegattaDTO(String name, ScoringSchemeType scoringScheme /*, List<CompetitorDTO> competitors*/) {
        super(name);
        this.scoringScheme = scoringScheme;
    }
    
    public Pair<SeriesDTO, FleetDTO> getSeriesAndFleet(RegattaAndRaceIdentifier raceIdentifier) {
        for (SeriesDTO s : series) {
            for (RaceColumnDTO raceColumn : s.getRaceColumns()) {
                FleetDTO fleet = raceColumn.getFleet(raceIdentifier);
                if (fleet != null) {
                    return new Pair<SeriesDTO, FleetDTO>(s, fleet);
                }
            }
        }
        return null;
    }

    public RegattaIdentifier getRegattaIdentifier() {
        return new RegattaName(getName());
    }
    
    /**
     * @return The start date of the first {@link #races Race}, or <code>null</code> if the start date isn't set
     */
    public Date getStartDate() {
        if(races.size()>0){
            return races.get(0).startOfRace;
        }
        return null;
    }
    
    /**
     * @return <code>true</code> if at least one race of the regatta is currently tracked, else it returns <code>false</code>
     */
    public boolean currentlyTracked() {
        boolean tracked = false;
        for (RaceDTO race : races) {
            tracked = race.isTracked;
            if (tracked) {
                break;
            }
        }
        return tracked;
    }
    
    /**
     * @return whether this regatta defines its local per-series result discarding rules; if so, any leaderboard based
     *         on the regatta has to respect this and has to use a result discarding rule implementation that
     *         keeps discards local to each series rather than spreading them across the entire leaderboard.
     */
    public boolean definesSeriesDiscardThresholds() {
        for (SeriesDTO s : series) {
            if (s.definesSeriesDiscardThresholds()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((scoringScheme == null) ? 0 : scoringScheme.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RegattaDTO other = (RegattaDTO) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (scoringScheme != other.scoringScheme)
            return false;
        return true;
    }
}
