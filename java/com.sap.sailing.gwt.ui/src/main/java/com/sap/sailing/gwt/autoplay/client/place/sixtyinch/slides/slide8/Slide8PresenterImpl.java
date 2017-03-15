package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;

public class Slide8PresenterImpl extends ConfiguredSlideBase<Slide8Place> implements Slide8View.Slide8Presenter {

    private Slide8View view;

    public Slide8PresenterImpl(Slide8Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide8View slide8ViewImpl) {
        super(place, clientFactory);
        this.view = slide8ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        getEventBus()
                .fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
