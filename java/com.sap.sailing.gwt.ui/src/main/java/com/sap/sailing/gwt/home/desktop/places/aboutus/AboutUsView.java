package com.sap.sailing.gwt.home.desktop.places.aboutus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AboutUsView extends Composite {
    private static AboutUsPageViewUiBinder uiBinder = GWT.create(AboutUsPageViewUiBinder.class);

    interface AboutUsPageViewUiBinder extends UiBinder<Widget, AboutUsView> {
    }

    public AboutUsView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
