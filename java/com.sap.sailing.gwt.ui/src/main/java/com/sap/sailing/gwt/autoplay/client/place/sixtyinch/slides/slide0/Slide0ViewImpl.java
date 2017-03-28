package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchLeaderBoard;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class Slide0ViewImpl extends ResizeComposite implements Slide0View {
    private static Slide0ViewImplUiBinder uiBinder = GWT.create(Slide0ViewImplUiBinder.class);

    interface Slide0ViewImplUiBinder extends UiBinder<Widget, Slide0ViewImpl> {
    }

    @UiField
    ResizableFlowPanel leaderBoardHolder;

    @UiField
    ResizableFlowPanel infoHolder;

    public Slide0ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(Slide1Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setLeaderBoard(SixtyInchLeaderBoard leaderboardPanel) {
        leaderBoardHolder.add(leaderboardPanel);
    }

}
