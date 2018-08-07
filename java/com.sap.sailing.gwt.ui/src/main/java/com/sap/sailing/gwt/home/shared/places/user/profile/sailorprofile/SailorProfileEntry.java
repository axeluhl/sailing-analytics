package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.List;

import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class SailorProfileEntry {

    private final String key;
    private final String name;
    private final List<CompetitorDTO> competitors;
    private final List<BadgeDTO> badges;
    private final List<BoatClassDTO> boatclasses;

    public SailorProfileEntry(String key, String name, List<CompetitorDTO> competitors, List<BadgeDTO> badges,
            List<BoatClassDTO> boatclasses) {
        super();
        this.key = key;
        this.name = name;
        this.competitors = competitors;
        this.badges = badges;
        this.boatclasses = boatclasses;
    }

    public String getKey() {
        return key;
    }

    public List<CompetitorDTO> getCompetitors() {
        return competitors;
    }

    public List<BadgeDTO> getBadges() {
        return badges;
    }

    public List<BoatClassDTO> getBoatclasses() {
        return boatclasses;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SailorProfileEntry other = (SailorProfileEntry) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

}
