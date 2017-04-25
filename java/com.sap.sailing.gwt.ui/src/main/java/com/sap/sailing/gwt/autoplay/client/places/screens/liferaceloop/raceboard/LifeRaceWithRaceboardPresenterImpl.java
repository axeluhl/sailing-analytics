package com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.raceboard;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.ConfiguredPresenter;
import com.sap.sailing.gwt.autoplay.client.app.sixtyinch.AutoPlayClientFactorySixtyInch;

public class LifeRaceWithRaceboardPresenterImpl extends ConfiguredPresenter<LifeRaceWithRaceboardPlace> implements LifeRaceWithRaceboardView.Slide7Presenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private LifeRaceWithRaceboardView view;
    ArrayList<CompetitorDTO> compList = new ArrayList<>();

    public LifeRaceWithRaceboardPresenterImpl(LifeRaceWithRaceboardPlace place, AutoPlayClientFactorySixtyInch clientFactory,
            LifeRaceWithRaceboardView LifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = LifeRaceWithRacemapViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        if (getPlace().getError() != null) {
            view.showErrorNoLive(this, panel, getPlace().getError());
            return;
        }

        RegattaAndRaceIdentifier lifeRace = getSlideCtx().getLifeRace();
        ArrayList<String> racesToShow = null;
        if (lifeRace != null) {
            racesToShow = new ArrayList<>();
            racesToShow.add(lifeRace.getRaceName());
        } else {
            view.showErrorNoLive(this, panel, new IllegalStateException("Na race is life"));
            return;
        }

        view.startingWith(this, panel, getPlace().getRaceMap());
    }

    @Override
    public void onStop() {
        view.onStop();
    }

}
