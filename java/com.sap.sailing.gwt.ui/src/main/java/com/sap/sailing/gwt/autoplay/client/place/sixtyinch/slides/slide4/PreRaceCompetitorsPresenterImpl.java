package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.ConfiguredSlideBase;

public class PreRaceCompetitorsPresenterImpl extends ConfiguredSlideBase<AbstractPreRaceCompetitorsPlace> implements PreRaceCompetitorsView.Slide4Presenter {

    private PreRaceCompetitorsView view;

    public PreRaceCompetitorsPresenterImpl(AbstractPreRaceCompetitorsPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            PreRaceCompetitorsView slide4ViewImpl) {
        super(place, clientFactory);
        this.view = slide4ViewImpl;

    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        view.startingWith(this, panel);
    }
}
