package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

public abstract class AbstractRaceEndWithImagesTop3Place extends Place {

    private GetMiniLeaderboardDTO leaderBoardDTO;
    private RegattaAndRaceIdentifier lifeRace;

    public GetMiniLeaderboardDTO getLeaderBoardDTO() {
        return leaderBoardDTO;
    }

    public void setLeaderBoardDTO(GetMiniLeaderboardDTO leaderBoardDTO) {
        this.leaderBoardDTO = leaderBoardDTO;
    }

    public RegattaAndRaceIdentifier getLastRace() {
        return lifeRace;
    }

    public void setLifeRace(RegattaAndRaceIdentifier lifeRace) {
        this.lifeRace = lifeRace;
    }
}
