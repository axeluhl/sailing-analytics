package com.sap.sailing.domain.anniversary;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class SimpleRaceInfo {
    private final RegattaAndRaceIdentifier identifier;
    private final Date startOfRace;
    private final String remoteName;

    public SimpleRaceInfo(RegattaAndRaceIdentifier identifier, Date startOfRace, String remoteName) {
        if (identifier == null || startOfRace == null) {
            throw new IllegalStateException("SimpleRaceInfo Data is not allowed to contain any null values!");
        }
        this.identifier = identifier;
        this.startOfRace = startOfRace;
        this.remoteName = remoteName;
    }

    public RegattaAndRaceIdentifier getIdentifier() {
        return identifier;
    }

    public Date getStartOfRace() {
        return startOfRace;
    }

    public String getRemoteName() {
        return remoteName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((startOfRace == null) ? 0 : startOfRace.hashCode());
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
        SimpleRaceInfo other = (SimpleRaceInfo) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (startOfRace == null) {
            if (other.startOfRace != null)
                return false;
        } else if (!startOfRace.equals(other.startOfRace))
            return false;
        return true;
    }

}