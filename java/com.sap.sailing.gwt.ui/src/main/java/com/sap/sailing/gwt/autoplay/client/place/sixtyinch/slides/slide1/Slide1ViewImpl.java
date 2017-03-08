package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class Slide1ViewImpl extends ResizeComposite implements Slide1View {
    private static Slide1ViewImplUiBinder uiBinder = GWT.create(Slide1ViewImplUiBinder.class);

    interface Slide1ViewImplUiBinder extends UiBinder<Widget, Slide1ViewImpl> {
    }

    @UiField
    Label ctxTest;

    public Slide1ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(Slide1Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setTestText(String leaderboardName) {
        ctxTest.setText(leaderboardName);
    }

}
