package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;

public class Slide0PresenterImpl extends ConfiguredSlideBase<Slide0Place> implements Slide0View.Slide0Presenter {

    private Slide0View view;

    public Slide0PresenterImpl(Slide0Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide0View slide0ViewImpl) {
        super(place, clientFactory);
        this.view = slide0ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        getEventBus()
                .fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
