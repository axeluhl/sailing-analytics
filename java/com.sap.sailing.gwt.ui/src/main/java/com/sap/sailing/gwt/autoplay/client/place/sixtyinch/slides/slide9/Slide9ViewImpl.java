package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class Slide9ViewImpl extends ResizeComposite implements Slide9View {
    private static Slide9ViewImplUiBinder uiBinder = GWT.create(Slide9ViewImplUiBinder.class);

    private static Dummy dummmyProvider = GWT.create(Dummy.class);
    @UiField
    com.google.gwt.user.client.ui.Image dummy;

    interface Dummy extends ClientBundle {
        ImageResource dummy();
    }

    interface Slide9ViewImplUiBinder extends UiBinder<Widget, Slide9ViewImpl> {
    }

    public Slide9ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        dummy.setUrl(dummmyProvider.dummy().getSafeUri());
    }

    @Override
    public void startingWith(Slide9Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

}
