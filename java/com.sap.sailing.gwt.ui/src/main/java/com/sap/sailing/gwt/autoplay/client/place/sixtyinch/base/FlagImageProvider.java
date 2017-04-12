package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreLeaderBoardWithImageViewImpl.ImageProvider;

public class FlagImageProvider implements ImageProvider {

    @Override
    public String getImageUrl(CompetitorDTO marked) {
        if (marked.getFlagImageURL() != null) {
            return marked.getFlagImageURL();
        }
        return "";
    }

}
