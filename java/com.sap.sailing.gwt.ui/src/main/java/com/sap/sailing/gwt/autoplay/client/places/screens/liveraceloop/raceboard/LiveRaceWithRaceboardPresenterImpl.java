package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.autoplay.client.app.classic.AutoPlayClientFactoryClassic;
import com.sap.sailing.gwt.autoplay.client.app.classic.ClassicConfiguredPresenter;

public class LiveRaceWithRaceboardPresenterImpl extends ClassicConfiguredPresenter<LiveRaceWithRaceboardPlace>
        implements LiveRaceWithRaceboardView.Slide7Presenter {
    protected static final int SWITCH_COMPETITOR_DELAY = 2000;
    private LiveRaceWithRaceboardView view;
    ArrayList<CompetitorDTO> compList = new ArrayList<>();

    public LiveRaceWithRaceboardPresenterImpl(LiveRaceWithRaceboardPlace place,
            AutoPlayClientFactoryClassic clientFactory, LiveRaceWithRaceboardView LifeRaceWithRacemapViewImpl) {
        super(place, clientFactory);
        this.view = LifeRaceWithRacemapViewImpl;
    }

    @Override
    public void startConfigured(AcceptsOneWidget panel) {
        if (getPlace().getError() != null) {
            view.showErrorNoLive(this, panel, getPlace().getError());
            return;
        }
        view.startingWith(this, panel, getPlace().getRaceBoardPanel());
    }

    @Override
    public void onStop() {
        view.onStop();
    }

}
