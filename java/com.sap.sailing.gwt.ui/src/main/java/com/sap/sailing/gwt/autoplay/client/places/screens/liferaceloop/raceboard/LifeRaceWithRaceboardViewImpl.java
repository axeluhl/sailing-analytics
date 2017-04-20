package com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class LifeRaceWithRaceboardViewImpl extends ResizeComposite implements LifeRaceWithRaceboardView {
    private static LifeRaceWithRacemapViewImplUiBinder uiBinder = GWT.create(LifeRaceWithRacemapViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel racemap;

    private Timer resizer;

    private RaceMap rawRaceMap;

    interface LifeRaceWithRacemapViewImplUiBinder extends UiBinder<Widget, LifeRaceWithRaceboardViewImpl> {
    }

    public LifeRaceWithRaceboardViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @Override
    public void showErrorNoLive(LifeRaceWithRaceboardPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error) {
        panel.setWidget(new Label("Could not load RaceMap: " + error.getMessage()));
    }

    @Override
    public void onStop() {
        resizer.cancel();
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap) {
        panel.setWidget(this);
        rawRaceMap = raceMap;
        racemap.add(raceMap);
        resizeMapOnceInitially();

        resizer.scheduleRepeating(100);
    }


    private void resizeMapOnceInitially() {
        new Timer() {
            @Override
            public void run() {
                racemap.onResize();
            }
        }.schedule(50);
    }

}
