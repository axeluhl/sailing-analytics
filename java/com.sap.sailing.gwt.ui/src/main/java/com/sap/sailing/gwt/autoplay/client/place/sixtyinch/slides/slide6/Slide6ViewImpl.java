package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class Slide6ViewImpl extends ResizeComposite implements Slide6View {
    private static Slide6ViewImplUiBinder uiBinder = GWT.create(Slide6ViewImplUiBinder.class);

    private static Dummy dummmyProvider = GWT.create(Dummy.class);
    @UiField
    com.google.gwt.user.client.ui.Image dummy;

    interface Dummy extends ClientBundle {
        ImageResource dummy();
    }

    interface Slide6ViewImplUiBinder extends UiBinder<Widget, Slide6ViewImpl> {
    }

    public Slide6ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        dummy.setUrl(dummmyProvider.dummy().getSafeUri());
    }

    @Override
    public void startingWith(Slide6Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

}
