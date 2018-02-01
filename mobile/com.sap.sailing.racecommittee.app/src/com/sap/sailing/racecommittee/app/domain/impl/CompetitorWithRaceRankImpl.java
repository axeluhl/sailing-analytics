package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sse.common.Color;
import com.sap.sse.common.Duration;

public class CompetitorWithRaceRankImpl extends CompetitorImpl {
    private static final long serialVersionUID = -6427316368214398951L;
    private List<RaceRankImpl> mRankList;

    public CompetitorWithRaceRankImpl(Serializable id, String name, Color color, String email, URI flagImage, DynamicTeam team, DynamicBoat boat,
        Double timeOnTimeFactor, Duration timeOnDistanceAllowancePerNauticalMile, String searchTag) {
        super(id, name, color, email, flagImage, team, boat, timeOnTimeFactor, timeOnDistanceAllowancePerNauticalMile, searchTag);

        mRankList = new ArrayList<>();
    }

    public void addRaceRank(RaceRankImpl raceRank) {
        mRankList.add(raceRank);
    }

    public long getRaceRank(String raceName) {
        long result = 0;
        for (RaceRankImpl raceRank : mRankList) {
            if (raceRank.getRace().equals(raceName)) {
                result = raceRank.getRank();
            }
        }
        return result;
    }
}
