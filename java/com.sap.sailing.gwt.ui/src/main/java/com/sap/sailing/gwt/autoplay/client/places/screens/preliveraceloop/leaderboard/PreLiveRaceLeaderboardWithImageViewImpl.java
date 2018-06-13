package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.SingleRaceLeaderboardPanel;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class PreLiveRaceLeaderboardWithImageViewImpl extends ResizeComposite implements PreLeaderboardWithImageView {
    private static IdleLeaderBoardWithFlagsViewImplUiBinder uiBinder = GWT
            .create(IdleLeaderBoardWithFlagsViewImplUiBinder.class);

    interface IdleLeaderBoardWithFlagsViewImplUiBinder extends UiBinder<Widget, PreLiveRaceLeaderboardWithImageViewImpl> {
    }

    @UiField
    ResizableFlowPanel leaderBoardHolder;

    @UiField
    ResizableFlowPanel infoHolder;

    @UiField
    Label bottomInfoPanel;

    @UiField
    Image image;
    @UiField
    Label subline1;
    @UiField
    Label subline2;
    @UiField
    Label subline3;

    private ImageProvider provider;

    public interface ImageProvider {
        String getImageUrl(CompetitorDTO marked);
    }

    public PreLiveRaceLeaderboardWithImageViewImpl(ImageProvider provider) {
        initWidget(uiBinder.createAndBindUi(this));
        this.provider = provider;
    }

    @Override
    public void startingWith(Slide1Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setLeaderBoard(SingleRaceLeaderboardPanel leaderboardPanel) {
        leaderBoardHolder.add(leaderboardPanel);
    }

    @Override
    public void onCompetitorSelect(CompetitorDTO selected) {
        image.setUrl(provider.getImageUrl(selected));
        final String boatName;
        if (selected.hasBoat() && (boatName = ((CompetitorWithBoatDTO) selected).getBoat().getName()) != null) {
            subline1.setText(boatName);
        } else {
            subline1.setText("");
        }
        if (selected.getName() != null) {
            subline2.setText(selected.getName());
        } else {
            subline2.setText("");
        }

        subline3.setText("");
    }

    @Override
    public void scrollLeaderBoardToTop() {
        leaderBoardHolder.getElement().setScrollTop(0);
    }

    @Override
    public void onStop() {
    }

    @Override
    public void nextRace(RegattaAndRaceIdentifier race) {
        bottomInfoPanel
                .setText(StringMessages.INSTANCE.next() + " " + race.getRegattaName() + " " + race.getRaceName());
    }

}
