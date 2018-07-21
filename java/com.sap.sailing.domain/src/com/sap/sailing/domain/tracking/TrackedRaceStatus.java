package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.TrackedRaceStatusEnum;

/**
 * Races undergo a variety of different states in our application. They may be created with a competitor list, maybe not
 * having a confirmed course yet and not even an expected start time. Later, the start time may be set. The race starts,
 * may be abandoned, restarted, running, finishing, finished, protests, completed, race committee confirmed.<p>
 * 
 * When a race is then re-loaded or re-connected while it is running, stored data may start to load, be loading, be done
 * loading. Live data may continue to be received, done receiving.<p>
 * 
 * The race may be archived (stored persistently in our database), loading from the DB, done loading from the DB. It has
 * to be possible to update an archived race in case something changes after it has been archived.<p>
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface TrackedRaceStatus extends Serializable {
    TrackedRaceStatusEnum getStatus();
    
    /**
     * Particularly interesting when the {@link #getStatus() status} is {@link TrackedRaceStatusEnum#LOADING}. Indicates the progress
     * of loading the race's tracking data.
     * 
     * @return a value in the range of 0.0 to 1.0 where 0.0 means no progress yet and 1.0 means loading has completed.
     *         1.0 will, however, hardly be seen in state {@link TrackedRaceStatusEnum#LOADING} because the status will probably
     *         already have transitioned to {@link TrackedRaceStatusEnum#TRACKING} or {@link TrackedRaceStatusEnum#FINISHED}.
     */
    double getLoadingProgress();
}
