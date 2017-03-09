package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SlideInitViewImpl extends ResizeComposite implements SlideInitView {
    private static SlideInitViewImplUiBinder uiBinder = GWT.create(SlideInitViewImplUiBinder.class);

    interface SlideInitViewImplUiBinder extends UiBinder<Widget, SlideInitViewImpl> {
    }

    @UiField
    SimplePanel content;

    public SlideInitViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void startingWith(SlideInitPresenter p, AcceptsOneWidget panel) {
        panel.setWidget(this);
    }

}
