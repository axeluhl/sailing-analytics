package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;

public class Slide6PresenterImpl extends SlideBase<Slide6Place> implements Slide6View.Slide6Presenter {

    private Slide6View view;

    public Slide6PresenterImpl(Slide6Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide6View slide6ViewImpl) {
        super(place, clientFactory);
        this.view = slide6ViewImpl;

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        eventBus.fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
