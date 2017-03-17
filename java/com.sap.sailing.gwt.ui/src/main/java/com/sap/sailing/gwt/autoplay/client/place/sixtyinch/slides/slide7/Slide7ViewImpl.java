package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;

public class Slide7ViewImpl extends ResizeComposite implements Slide7View {
    private static Slide7ViewImplUiBinder uiBinder = GWT.create(Slide7ViewImplUiBinder.class);

    @UiField
    ResizableFlowPanel racemap;

    private AcceptsOneWidget panel;

    interface Slide7ViewImplUiBinder extends UiBinder<Widget, Slide7ViewImpl> {
    }

    public Slide7ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(Slide7Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
        this.panel = panel;
    }

    @Override
    public void setRaceMap(Widget raceboardPerspective) {
        panel.setWidget(raceboardPerspective);
    }

    @Override
    public void showErrorNoLive() {
        panel.setWidget(new Label("No life race"));
    }

}
