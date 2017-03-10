package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;
import com.sap.sailing.gwt.home.mobile.partials.minileaderboard.MinileaderboardBox;

public class Slide1ViewImpl extends ResizeComposite implements Slide1View {
    private static Slide1ViewImplUiBinder uiBinder = GWT.create(Slide1ViewImplUiBinder.class);

    interface Slide1ViewImplUiBinder extends UiBinder<Widget, Slide1ViewImpl> {
    }

    @UiField
    SimplePanel miniLeaderBoard;

    public Slide1ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(Slide1Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setLeaderBoardDTO(GetMiniLeaderboardDTO leaderBoardDTO) {
        MinileaderboardBox miniBox = new MinileaderboardBox(false);
        miniBox.setData(leaderBoardDTO);
        miniLeaderBoard.setWidget(miniBox);
    }


}
