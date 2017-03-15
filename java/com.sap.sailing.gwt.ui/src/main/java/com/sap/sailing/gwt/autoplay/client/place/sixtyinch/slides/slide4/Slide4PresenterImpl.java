package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;

public class Slide4PresenterImpl extends ConfiguredSlideBase<Slide4Place> implements Slide4View.Slide4Presenter {

    private Slide4View view;

    public Slide4PresenterImpl(Slide4Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide4View slide4ViewImpl) {
        super(place, clientFactory);
        this.view = slide4ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        getEventBus()
                .fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
