package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

public class RaceInLeaderboardDTO implements IsSerializable {
    private String raceColumnName;
    private boolean medalRace;
    private RaceIdentifier trackedRaceIdentifier;

    public String getRaceColumnName() {
        return raceColumnName;
    }

    public void setRaceColumnName(String raceColumnName) {
        this.raceColumnName = raceColumnName;
    }

    public boolean isMedalRace() {
        return medalRace;
    }

    public void setMedalRace(boolean medalRace) {
        this.medalRace = medalRace;
    }

    public boolean isTrackedRace() {
        return trackedRaceIdentifier != null;
    }

    public void setRaceIdentifier(RaceIdentifier raceIdentifier) {
        this.trackedRaceIdentifier = raceIdentifier;
    }

    /**
     * @return a non-<code>null</code> race identifier if this column represents a <em>tracked</em> race. Such a race's
     *         data can be obtained from the server in great detail, as opposed to non-tracked races for which only
     *         result points may have been entered manually.
     */
    public RaceIdentifier getRaceIdentifier() {
        return trackedRaceIdentifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (medalRace ? 1231 : 1237);
        result = prime * result + ((raceColumnName == null) ? 0 : raceColumnName.hashCode());
        result = prime * result + ((trackedRaceIdentifier == null) ? 0 : trackedRaceIdentifier.hashCode());
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
        RaceInLeaderboardDTO other = (RaceInLeaderboardDTO) obj;
        if (medalRace != other.medalRace)
            return false;
        if (raceColumnName == null) {
            if (other.raceColumnName != null)
                return false;
        } else if (!raceColumnName.equals(other.raceColumnName))
            return false;
        if (trackedRaceIdentifier == null) {
            if (other.trackedRaceIdentifier != null)
                return false;
        } else if (!trackedRaceIdentifier.equals(other.trackedRaceIdentifier))
            return false;
        return true;
    }
}
