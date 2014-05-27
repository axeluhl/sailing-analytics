package com.sap.sailing.gwt.home.client.app.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.Stage;

public class TabletAndDesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopStartView> {
    }

    @UiField Stage stage;

    public TabletAndDesktopStartView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
