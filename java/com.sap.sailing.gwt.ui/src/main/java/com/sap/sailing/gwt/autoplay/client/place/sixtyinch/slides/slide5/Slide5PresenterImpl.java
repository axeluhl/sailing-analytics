package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;

public class Slide5PresenterImpl extends ConfiguredSlideBase<Slide5Place> implements Slide5View.Slide5Presenter {

    private Slide5View view;

    public Slide5PresenterImpl(Slide5Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide5View slide5ViewImpl) {
        super(place, clientFactory);
        this.view = slide5ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        getEventBus()
                .fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
