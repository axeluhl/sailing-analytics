package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

public interface Slide1View {
    void startingWith(Slide1Presenter p, AcceptsOneWidget panel);
    public interface Slide1Presenter {
    }

    void setLeaderBoardDTO(GetMiniLeaderboardDTO leaderBoardDTO);
}
