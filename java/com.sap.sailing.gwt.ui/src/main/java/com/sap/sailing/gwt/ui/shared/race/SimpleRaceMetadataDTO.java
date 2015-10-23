package com.sap.sailing.gwt.ui.shared.race;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;

public class SimpleRaceMetadataDTO implements DTO, Comparable<SimpleRaceMetadataDTO> {
    public enum RaceViewState {
        PLANNED {       // no start time set
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsPlanned();
            }
        },
        SCHEDULED {     // the start time is set and in the future
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsScheduled(); // fallback representation
            }
        },
        RUNNING {       // start time is the past and end time is not set or in the future
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsRunning(); // fallback representation
            }
        },
        FINISHED {      // the end time is set and is in the past
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsFinished();
            }
        },
        POSTPONED {     // the start has been postponed
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsPostponed();
            }
        },
        ABANDONED {      // the running racing has been abandoned
            @Override
            public String getLabel() {
                return StringMessages.INSTANCE.raceIsCanceled();
            }
        };

        public abstract String getLabel();
    }
    
    public enum RaceTrackingState {
        NOT_TRACKED,             // No tracking data -> probably just managed by race committee app
        TRACKED_NO_VALID_DATA,   // tracking is connected but the required data for displaying the race viewer is not available
        TRACKED_VALID_DATA       // tracking is connected and all required data for displaying the race viewer is available
    }
    
    private String leaderboardName;
    private RegattaAndRaceIdentifier regattaAndRaceIdentifier;
    private String raceName;
    private Date start;
    private RaceViewState state;
    private RaceTrackingState trackingState;
    
    protected SimpleRaceMetadataDTO() {
    }
    
    public SimpleRaceMetadataDTO(String leaderboardName, RegattaAndRaceIdentifier regattaAndRaceIdentifier, String raceName) {
        this.leaderboardName = leaderboardName;
        this.regattaAndRaceIdentifier = regattaAndRaceIdentifier;
        this.raceName = raceName;
    }
    
    public String getLeaderboardName() {
        return leaderboardName;
    }

    public RegattaAndRaceIdentifier getRegattaAndRaceIdentifier() {
        return regattaAndRaceIdentifier;
    }

    public String getRaceName() {
        return raceName;
    }
    
    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }
    
    public RaceViewState getViewState() {
        return state;
    }

    public void setViewState(RaceViewState state) {
        this.state = state;
    }

    public RaceTrackingState getTrackingState() {
        return trackingState;
    }

    public void setTrackingState(RaceTrackingState trackingState) {
        this.trackingState = trackingState;
    }
    
    @Override
    public int compareTo(SimpleRaceMetadataDTO o) {
        final int compareByRaceName = new NaturalComparator().compare(getRaceName(), o.getRaceName());
        return compareByRaceName != 0 ? compareByRaceName : Util.compareToWithNull(getStart(), o.getStart(), true);
    }
}
