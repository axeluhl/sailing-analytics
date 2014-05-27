package com.sap.sailing.gwt.home.client.place.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.stage.Stage;

public class SmartphoneStartView extends Composite implements StartView {
    private static StartPageMobileViewUiBinder uiBinder = GWT.create(StartPageMobileViewUiBinder.class);

    interface StartPageMobileViewUiBinder extends UiBinder<Widget, SmartphoneStartView> {
    }

    @UiField Stage stage;

    public SmartphoneStartView() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }
}
