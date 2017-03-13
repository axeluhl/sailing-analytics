package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class Slide4ViewImpl extends ResizeComposite implements Slide4View {
    private static Slide4ViewImplUiBinder uiBinder = GWT.create(Slide4ViewImplUiBinder.class);

    private static Dummy dummmyProvider = GWT.create(Dummy.class);
    @UiField
    com.google.gwt.user.client.ui.Image dummy;

    interface Dummy extends ClientBundle {
        ImageResource dummy();
    }

    interface Slide4ViewImplUiBinder extends UiBinder<Widget, Slide4ViewImpl> {
    }

    public Slide4ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        dummy.setUrl(dummmyProvider.dummy().getSafeUri());
    }

    @Override
    public void startingWith(Slide4Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

}
