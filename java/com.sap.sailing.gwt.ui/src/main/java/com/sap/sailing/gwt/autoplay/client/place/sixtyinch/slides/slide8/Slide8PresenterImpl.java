package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;

public class Slide8PresenterImpl extends SlideBase<Slide8Place> implements Slide8View.Slide8Presenter {

    private Slide8View view;

    public Slide8PresenterImpl(Slide8Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide8View slide8ViewImpl) {
        super(place, clientFactory);
        this.view = slide8ViewImpl;

    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        eventBus.fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
