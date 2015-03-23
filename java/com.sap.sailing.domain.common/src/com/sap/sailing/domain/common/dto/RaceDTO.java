package com.sap.sailing.domain.common.dto;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TimingConstants;

/**
 * Master data about a single race that is to be transferred to the client.<p>
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RaceDTO extends NamedDTO {
    private static final long serialVersionUID = 2613189982608149975L;

    public enum RaceLiveState { NOT_TRACKED, TRACKED, TRACKED_AND_LIVE, TRACKED_BUT_NOT_SCHEDULED };

    /**
     * Tells if this race is currently being tracked, meaning that a {@link RaceTracker} is
     * listening for incoming GPS fixes, mark passings etc., to update a {@link TrackedRace} object
     * accordingly.
     */
    public boolean isTracked;

    public Date startOfRace;
    public Date endOfRace;
    public RaceStatusDTO status;

    public PlacemarkOrderDTO places;

    public TrackedRaceDTO trackedRace;
    public TrackedRaceStatisticsDTO trackedRaceStatistics;

    private String regattaName;
    public String boatClass;
    
    public RaceDTO() {}

    public RaceDTO(RegattaAndRaceIdentifier raceIdentifier) {
        this(raceIdentifier, null, false);
    }

    public RaceDTO(RegattaAndRaceIdentifier raceIdentifier, TrackedRaceDTO trackedRace, boolean isCurrentlyTracked) {
        super(raceIdentifier.getRaceName());
        this.regattaName = raceIdentifier.getRegattaName();
        this.trackedRace = trackedRace;
        this.isTracked = isCurrentlyTracked;
    }

    public RaceLiveState getLiveState(long serverTimePointAsMillis) {
        RaceLiveState result = RaceLiveState.NOT_TRACKED;         
        if(trackedRace != null && trackedRace.hasGPSData && trackedRace.hasWindData) {
            result = RaceLiveState.TRACKED;
            if(isLive(serverTimePointAsMillis)) {
                result = RaceLiveState.TRACKED_AND_LIVE;
                if(startOfRace == null) {
                    result = RaceLiveState.TRACKED_BUT_NOT_SCHEDULED;
                }                    
            }
        }
        return result;
    }

    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return new RegattaNameAndRaceName(regattaName, getName());
    }

    public String getRegattaName() {
        return regattaName;
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
        result = prime * result + ((boatClass == null) ? 0 : boatClass.hashCode());
        result = prime * result + ((endOfRace == null) ? 0 : endOfRace.hashCode());
        result = prime * result + (isTracked ? 1231 : 1237);
        result = prime * result + ((places == null) ? 0 : places.hashCode());
        result = prime * result + ((regattaName == null) ? 0 : regattaName.hashCode());
        result = prime * result + ((startOfRace == null) ? 0 : startOfRace.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        RaceDTO other = (RaceDTO) obj;
        if (boatClass == null) {
            if (other.boatClass != null)
                return false;
        } else if (!boatClass.equals(other.boatClass))
            return false;
        if (endOfRace == null) {
            if (other.endOfRace != null)
                return false;
        } else if (!endOfRace.equals(other.endOfRace))
            return false;
        if (isTracked != other.isTracked)
            return false;
        if (places == null) {
            if (other.places != null)
                return false;
        } else if (!places.equals(other.places))
            return false;
        if (regattaName == null) {
            if (other.regattaName != null)
                return false;
        } else if (!regattaName.equals(other.regattaName))
            return false;
        if (startOfRace == null) {
            if (other.startOfRace != null)
                return false;
        } else if (!startOfRace.equals(other.startOfRace))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (trackedRace == null) {
            if (other.trackedRace != null)
                return false;
        } else if (!trackedRace.equals(other.trackedRace))
            return false;
        return true;
    }
}
