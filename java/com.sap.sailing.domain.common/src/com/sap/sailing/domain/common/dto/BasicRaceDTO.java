package com.sap.sailing.domain.common.dto;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TimingConstants;

/**
 * Master data about a single race that is to be transferred to the client. Holds only timing and a bit
 * of information about the tracked race, if any. See also {@link RaceDTO} for a more comprehensive set
 * of data about a race to be serialized to a client.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class BasicRaceDTO extends NamedDTO {
    private static final long serialVersionUID = -7884808503795229609L;
    
    public Date startOfRace;
    public Date endOfRace;
    public Date raceFinishedTime;
    public TrackedRaceDTO trackedRace;

    public BasicRaceDTO() {} // for GWT serialization only

    public BasicRaceDTO(RegattaAndRaceIdentifier raceIdentifier, TrackedRaceDTO trackedRace) {
        super(raceIdentifier.getRaceName());
        this.trackedRace = trackedRace;
    }

    /**
     * @see {@link TrackedRace#isLive} for further explanation.
     * @param serverTimePointAsMillis
     *            the time point (in server clock time) at which to determine whether the race is/was live
     * @return <code>true</code> if <code>serverTimePointAsMillis</code> is between (inclusively) the start and end time
     *         point of the "live" interval as defined above.
     */
    public boolean isLive(long serverTimePointAsMillis) {
        final Date startOfLivePeriod;
        final Date endOfLivePeriod;
        if (trackedRace == null || !trackedRace.hasGPSData || !trackedRace.hasWindData) {
            startOfLivePeriod = null;
            endOfLivePeriod = null;
        } else {
            if (startOfRace == null) {
                startOfLivePeriod = trackedRace.startOfTracking;
            } else {
                startOfLivePeriod = new Date(startOfRace.getTime() - TimingConstants.PRE_START_PHASE_DURATION_IN_MILLIS);
            }
            if (endOfRace == null) {
                if (trackedRace.timePointOfNewestEvent != null) {
                    endOfLivePeriod = new Date(trackedRace.timePointOfNewestEvent.getTime()
                            + TimingConstants.IS_LIVE_GRACE_PERIOD_IN_MILLIS);
                } else {
                    endOfLivePeriod = null;
                }
            } else {
                endOfLivePeriod = new Date(endOfRace.getTime() + TimingConstants.IS_LIVE_GRACE_PERIOD_IN_MILLIS);
            }
        }
        
        // if an empty timepoint is given then take the start of the race
        if (serverTimePointAsMillis == 0) {
            serverTimePointAsMillis = startOfLivePeriod.getTime()+1;
        }
        
        // whenLastTrackedRaceWasLive is null if there is no tracked race for fleet, or the tracked race hasn't started yet at the server time
        // when this DTO was assembled, or there were no GPS or wind data
        final boolean result =
                startOfLivePeriod != null &&
                endOfLivePeriod != null &&
                startOfLivePeriod.getTime() <= serverTimePointAsMillis &&
                serverTimePointAsMillis <= endOfLivePeriod.getTime();
        return result;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((endOfRace == null) ? 0 : endOfRace.hashCode());
        result = prime * result + ((startOfRace == null) ? 0 : startOfRace.hashCode());
        result = prime * result + ((trackedRace == null) ? 0 : trackedRace.hashCode());
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
        BasicRaceDTO other = (BasicRaceDTO) obj;
        if (endOfRace == null) {
            if (other.endOfRace != null)
                return false;
        } else if (!endOfRace.equals(other.endOfRace))
            return false;
        if (startOfRace == null) {
            if (other.startOfRace != null)
                return false;
        } else if (!startOfRace.equals(other.startOfRace))
            return false;
        if (trackedRace == null) {
            if (other.trackedRace != null)
                return false;
        } else if (!trackedRace.equals(other.trackedRace))
            return false;
        return true;
    }
}
