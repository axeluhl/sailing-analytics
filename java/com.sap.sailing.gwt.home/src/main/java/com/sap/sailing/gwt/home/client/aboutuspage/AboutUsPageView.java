package com.sap.sailing.gwt.home.client.aboutuspage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class AboutUsPageView extends Composite implements AboutUsPagePresenter.MyView {
    private static AboutUsPageViewUiBinder uiBinder = GWT.create(AboutUsPageViewUiBinder.class);

    interface AboutUsPageViewUiBinder extends UiBinder<Widget, AboutUsPageView> {
    }

    public AboutUsPageView() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {
    }

    @Override
    public void removeFromSlot(Object slot, IsWidget content) {
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
    }
}

