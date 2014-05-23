package com.sap.sailing.gwt.home.client.app.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SmartphoneStartView extends Composite implements StartView {
    private static StartPageMobileViewUiBinder uiBinder = GWT.create(StartPageMobileViewUiBinder.class);

    interface StartPageMobileViewUiBinder extends UiBinder<Widget, SmartphoneStartView> {
    }

    public SmartphoneStartView() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }
}
