package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide3;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;

public class Slide3PresenterImpl extends ConfiguredSlideBase<Slide3Place> implements Slide3View.Slide3Presenter {

    private Slide3View view;

    public Slide3PresenterImpl(Slide3Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide3View slide3ViewImpl) {
        super(place, clientFactory);
        this.view = slide3ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        getEventBus()
                .fireEvent(new SlideHeaderEvent("i18n Whats next?", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
