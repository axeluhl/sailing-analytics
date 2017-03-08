package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class Slide2ViewImpl extends ResizeComposite implements Slide2View {
    private static Slide1ViewImplUiBinder uiBinder = GWT.create(Slide1ViewImplUiBinder.class);

    interface Slide1ViewImplUiBinder extends UiBinder<Widget, Slide2ViewImpl> {
    }

    public Slide2ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(Slide2Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

}
