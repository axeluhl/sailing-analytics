package com.sap.sailing.domain.common.dto;

import java.util.Date;

import com.sap.sailing.domain.common.RaceTimesCalculationUtil;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TimingConstants;
import com.sap.sse.common.Util;
import com.sap.sse.security.shared.dto.NamedDTO;

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
    public Date raceFinishingTime;
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
        Date timePoint = null;
        if (serverTimePointAsMillis != 0) {
            timePoint = new Date(serverTimePointAsMillis);
        } else if (trackedRace != null && trackedRace.startOfTracking != null) {
            timePoint = trackedRace.startOfTracking;
        } else if (startOfRace != null) {
            timePoint = new Date(startOfRace.getTime() - TimingConstants.PRE_START_PHASE_DURATION_IN_MILLIS + 1);
        }

        if (trackedRace != null && timePoint != null && trackedRace.hasGPSData && trackedRace.hasWindData) {
            Util.Pair<Date, Date> minMax = RaceTimesCalculationUtil.calculateRaceMinMax(timePoint,
                    trackedRace.startOfTracking, startOfRace, raceFinishingTime, raceFinishedTime, endOfRace,
                    trackedRace.endOfTracking, TimingConstants.PRE_START_PHASE_DURATION_IN_MILLIS,
                    RaceTimesCalculationUtil.MAX_TIME_AFTER_RACE_END,
                    TimingConstants.IS_LIVE_GRACE_PERIOD_IN_MILLIS + trackedRace.delayToLiveInMs);
            Date min = minMax.getA() != null ? new Date(minMax.getA().getTime()) : null;
            Date max = minMax.getB() != null ? new Date(minMax.getB().getTime()) : null;

            // We are live if at is in between min and max
            if (min != null && max != null) {
                return !min.after(timePoint) && !timePoint.after(max);
            }
        }
        return false;
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
