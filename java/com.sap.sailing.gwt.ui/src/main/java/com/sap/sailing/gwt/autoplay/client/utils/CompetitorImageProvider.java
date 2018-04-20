package com.sap.sailing.gwt.autoplay.client.utils;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreLiveRaceLeaderBoardWithImageViewImpl.ImageProvider;

public class CompetitorImageProvider implements ImageProvider {

    @Override
    public String getImageUrl(CompetitorWithBoatDTO marked) {
        if (marked.getImageURL() != null) {
            return marked.getImageURL();
        }
        return "";
    }

}
