package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.leaderboard;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

public abstract class AbstractPreRaceLeaderBoardWithImagePlace extends Place {

    private GetMiniLeaderboardDTO leaderBoardDTO;

    public GetMiniLeaderboardDTO getLeaderBoardDTO() {
        return leaderBoardDTO;
    }

    public void setLeaderBoardDTO(GetMiniLeaderboardDTO leaderBoardDTO) {
        this.leaderBoardDTO = leaderBoardDTO;
    }
}
