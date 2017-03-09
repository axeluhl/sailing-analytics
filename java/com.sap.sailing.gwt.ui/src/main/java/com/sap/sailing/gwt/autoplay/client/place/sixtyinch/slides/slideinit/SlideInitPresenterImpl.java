package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SlideHeaderEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.SlideBase;

public class SlideInitPresenterImpl extends SlideBase<SlideInitPlace> implements SlideInitView.SlideInitPresenter {

    private SlideInitView view;

    public SlideInitPresenterImpl(SlideInitPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            SlideInitView slide1ViewImpl) {
        super(place, clientFactory);
        this.view = slide1ViewImpl;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {

        eventBus.fireEvent(
                new SlideHeaderEvent("Loading event data", getSlideCtx().getSettings().getLeaderBoardName()));
        view.startingWith(this, panel);
    }
}
