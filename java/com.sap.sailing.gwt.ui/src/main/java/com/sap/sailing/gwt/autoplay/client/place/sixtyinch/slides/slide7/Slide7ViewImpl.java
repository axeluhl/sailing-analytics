package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class Slide7ViewImpl extends ResizeComposite implements Slide7View {
    private static Slide7ViewImplUiBinder uiBinder = GWT.create(Slide7ViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel racemap;

    interface Slide7ViewImplUiBinder extends UiBinder<Widget, Slide7ViewImpl> {
    }

    public Slide7ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel, RaceMap raceMap) {
        panel.setWidget(this);
        racemap.add(raceMap);
    }

    @Override
    public void showErrorNoLive(Slide7PresenterImpl slide7PresenterImpl, AcceptsOneWidget panel, Throwable error) {
        panel.setWidget(new Label("Could not load RaceMap"));
    }

}
