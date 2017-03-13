package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;

public class Slide5ViewImpl extends ResizeComposite implements Slide5View {
    private static Slide5ViewImplUiBinder uiBinder = GWT.create(Slide5ViewImplUiBinder.class);

    private static Dummy dummmyProvider = GWT.create(Dummy.class);
    @UiField
    com.google.gwt.user.client.ui.Image dummy;

    interface Dummy extends ClientBundle {
        ImageResource dummy();
    }

    interface Slide5ViewImplUiBinder extends UiBinder<Widget, Slide5ViewImpl> {
    }

    public Slide5ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        dummy.setUrl(dummmyProvider.dummy().getSafeUri());
    }

    @Override
    public void startingWith(Slide5Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

}
