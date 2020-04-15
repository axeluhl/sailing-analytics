package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/**
 * Contains the result of a single statistic, to allow asynchronous loading of statistics without blocking loading the
 * sailor profile or the events
 **/
public class SailorProfileStatisticDTO implements Result, Serializable {
    private static final long serialVersionUID = 2924378586764418626L;
    // keep as specified as possible to save gwt compiler time
    private HashMap<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> result = new HashMap<>();
    private String dataMiningQuery;

    // GWTSerialisation only
    protected SailorProfileStatisticDTO() {
        super();
    }

    public SailorProfileStatisticDTO(Map<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> result,
            String dataMiningQuery) {
        this.result.putAll(result);
        this.dataMiningQuery = dataMiningQuery;
    }

    public HashMap<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> getResult() {
        return result;
    }

    public String getDataMiningQuery() {
        return dataMiningQuery;
    }

    public static class SingleEntry implements Serializable {
        private static final long serialVersionUID = -7722750678632551505L;

        @GwtIncompatible
        public SingleEntry(Double value, RegattaAndRaceIdentifier relatedRaceOrNull, TimePoint relatedTimePointOrNull,
                TimePoint relatedRaceStartTimePointOrNull, String leaderboardNameOrNull,
                String leaderboardGroupNameOrNull, UUID eventIdOrNull, String raceNameOrNull) {
            super();
            this.value = value;
            this.relatedRaceOrNull = relatedRaceOrNull;
            this.leaderboardNameOrNull = leaderboardNameOrNull;
            this.leaderboardGroupNameOrNull = leaderboardGroupNameOrNull;
            this.eventIdOrNull = eventIdOrNull;
            this.raceNameOrNull = raceNameOrNull;
            if (relatedTimePointOrNull != null) {
                // not all TimePoints are GWT compatible, ensure we have a compatible one!
                if (relatedTimePointOrNull instanceof MillisecondsTimePoint) {
                    this.relatedTimePointOrNull = (MillisecondsTimePoint) relatedTimePointOrNull;
                } else {
                    this.relatedTimePointOrNull = new MillisecondsTimePoint(relatedTimePointOrNull.asMillis());
                }
            }
            if (relatedRaceStartTimePointOrNull != null) {
                if (relatedRaceStartTimePointOrNull instanceof MillisecondsTimePoint) {
                    this.relatedRaceStartTimePointOrNull = (MillisecondsTimePoint) relatedRaceStartTimePointOrNull;
                } else {
                    this.relatedRaceStartTimePointOrNull = new MillisecondsTimePoint(
                            relatedRaceStartTimePointOrNull.asMillis());
                }
            }
        }

        // GWTSerialisation only
        protected SingleEntry() {
        }

        private Double value;
        private RegattaAndRaceIdentifier relatedRaceOrNull;
        // not generic, to reduce possible permutations for gwt compiler
        private MillisecondsTimePoint relatedTimePointOrNull;
        private MillisecondsTimePoint relatedRaceStartTimePointOrNull;
        private String leaderboardNameOrNull;
        private String leaderboardGroupNameOrNull;
        private String raceNameOrNull;
        private UUID eventIdOrNull;

        /**
         * All values will be in SI Units if not otherwise stated in the Type documentation
         */
        public Double getValue() {
            return value;
        }

        public RegattaAndRaceIdentifier getRelatedRaceOrNull() {
            return relatedRaceOrNull;
        }

        public MillisecondsTimePoint getRelatedTimePointOrNull() {
            return relatedTimePointOrNull;
        }

        @Override
        public String toString() {
            return "SingleEntry [value=" + value + ", relatedRaceOrNull=" + relatedRaceOrNull
                    + ", relatedTimePointOrNull=" + relatedTimePointOrNull + ", relatedRaceStartTimePointOrNull="
                    + relatedRaceStartTimePointOrNull + ", leaderboardNameOrNull=" + leaderboardNameOrNull
                    + ", leaderboardGroupNameOrNull=" + leaderboardGroupNameOrNull + ", raceNameOrNull="
                    + raceNameOrNull + ", eventIdOrNull=" + eventIdOrNull + "]";
        }

        public String getLeaderboardNameOrNull() {
            return leaderboardNameOrNull;
        }

        public UUID getEventIdOrNull() {
            return eventIdOrNull;
        }

        public String getLeaderboardGroupNameOrNull() {
            return leaderboardGroupNameOrNull;
        }

        public MillisecondsTimePoint getRelatedRaceStartTimePointOrNull() {
            return relatedRaceStartTimePointOrNull;
        }

        public String getRaceNameOrNull() {
            return raceNameOrNull;
        }

    }
}
