package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;

public class Slide7PresenterImpl extends ConfiguredSlideBase<Slide7Place> implements Slide7View.Slide7Presenter {
    private Slide7View view;

    public Slide7PresenterImpl(Slide7Place place, AutoPlayClientFactorySixtyInch clientFactory,
            Slide7View slide7ViewImpl) {
        super(place, clientFactory);
        this.view = slide7ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        if (getPlace().getError() != null) {
            view.showErrorNoLive(this, panel, getPlace().getError());
        } else {
            view.startingWith(this, panel, getPlace().getRaceMap());
        }
    }
}
