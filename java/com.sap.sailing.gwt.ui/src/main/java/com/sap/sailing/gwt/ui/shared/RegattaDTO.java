package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.ScoringSchemeType;

public class RegattaDTO extends NamedDTO implements IsSerializable {
    /**
     * May be <code>null</code> in case the boat class is not known
     */
    public BoatClassDTO boatClass;
    public List<RaceWithCompetitorsDTO> races;
    public List<SeriesDTO> series;
    public ScoringSchemeType scoringScheme;

    public RegattaDTO() {}

    public RegattaDTO(String name, ScoringSchemeType scoringScheme /*, List<CompetitorDTO> competitors*/) {
        super(name);
        this.name = name;
        this.scoringScheme = scoringScheme;
    }

    /**
     * @return The start date of the first {@link #races Race}, or <code>null</code> if the start date isn't set
     */
    public Date getStartDate() {
        return races.get(0).startOfRace;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (scoringScheme != other.scoringScheme)
            return false;
        return true;
    }
}
