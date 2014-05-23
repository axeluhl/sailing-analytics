package com.sap.sailing.gwt.home.client.app.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class TabletAndDesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    @UiField
    Label bannerSeriesName;
    @UiField
    Label bannerName;
    @UiField
    Label bannerLocation;

    @UiField
    Label upcomingMessage;
    @UiField
    Label upcomingName;
    @UiField
    Label upcomingAction;

    interface StartPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopStartView> {
    }

    public TabletAndDesktopStartView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
