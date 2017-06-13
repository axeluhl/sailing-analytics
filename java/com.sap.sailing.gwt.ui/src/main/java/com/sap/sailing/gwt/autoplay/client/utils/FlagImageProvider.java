package com.sap.sailing.gwt.autoplay.client.utils;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreLiveRaceLeaderBoardWithImageViewImpl.ImageProvider;

public class FlagImageProvider implements ImageProvider {

    @Override
    public String getImageUrl(CompetitorDTO marked) {
        if (marked.getFlagImageURL() != null) {
            return marked.getFlagImageURL();
        }
        return "";
    }

}
