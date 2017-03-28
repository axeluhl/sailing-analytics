package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class Slide2ViewImpl extends Composite implements Slide2View {
    private static Slide3ViewImplUiBinder uiBinder = GWT.create(Slide3ViewImplUiBinder.class);


    @UiField
    SimplePanel mainPanelUi;

    interface Dummy extends ClientBundle {
        ImageResource dummy();
    }

    interface Slide3ViewImplUiBinder extends UiBinder<Widget, Slide2ViewImpl> {
    }

    public Slide2ViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(Slide2Presenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

    @Override
    public void setBackgroudImage(String string) {
        mainPanelUi.getElement().getStyle().setBackgroundImage(string);
    }
}
