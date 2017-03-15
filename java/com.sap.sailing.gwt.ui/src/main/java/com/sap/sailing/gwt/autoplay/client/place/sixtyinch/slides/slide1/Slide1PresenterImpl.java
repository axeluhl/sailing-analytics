package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.GetMiniLeaderboardDTO;

public class Slide1PresenterImpl extends ConfiguredSlideBase<Slide1Place> implements Slide1View.Slide1Presenter {

    private Slide1View view;

    public Slide1PresenterImpl(Slide1Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide1View slide1ViewImpl) {
        super(place, clientFactory);
        this.view = slide1ViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        getEventBus()
                .fireEvent(new SlideHeaderEvent("i18n 5 Races Rank", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);

        getEventBus().addHandler(MiniLeaderboardUpdatedEvent.TYPE, new MiniLeaderboardUpdatedEvent.Handler() {

            @Override
            public void handleNoOpEvent(MiniLeaderboardUpdatedEvent e) {
                GetMiniLeaderboardDTO leaderBoardDTO = getClientFactory().getSlideCtx().getMiniLeaderboardDTO();
                view.setLeaderBoardDTO(leaderBoardDTO);
            }

        });
        GetMiniLeaderboardDTO leaderBoardDTO = getClientFactory().getSlideCtx().getMiniLeaderboardDTO();
        view.setLeaderBoardDTO(leaderBoardDTO);
    }
}
