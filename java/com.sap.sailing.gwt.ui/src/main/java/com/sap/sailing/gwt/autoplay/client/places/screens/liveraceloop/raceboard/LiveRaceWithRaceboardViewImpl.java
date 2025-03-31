package com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class LiveRaceWithRaceboardViewImpl extends ResizeComposite implements LiveRaceWithRaceboardView {
    private static LifeRaceWithRacemapViewImplUiBinder uiBinder = GWT.create(LifeRaceWithRacemapViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel racemap;

    interface LifeRaceWithRacemapViewImplUiBinder extends UiBinder<Widget, LiveRaceWithRaceboardViewImpl> {
    }

    public LiveRaceWithRaceboardViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }


    @Override
    public void showErrorNoLive(LiveRaceWithRaceboardPresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error) {
        panel.setWidget(new Label("Could not load RaceMap: " + error.getMessage()));
    }

    public void onStop() {
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceBoardPanel raceMap) {
        panel.setWidget(this);
        racemap.add(raceMap);
        raceMap.getElement().getStyle().setHeight(100, Unit.PCT);
        new Timer() {
            @Override
            public void run() {
                raceMap.onResize();
            }
        }.schedule(2000);
        ;
    }
}
