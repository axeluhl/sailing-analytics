package com.sap.sailing.gwt.home.client.place.solutions.analytics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TabletAndDesktopSailingAnalyticsView extends Composite implements SailingAnalyticsView {
    private static SailingAnalyticsPageViewUiBinder uiBinder = GWT.create(SailingAnalyticsPageViewUiBinder.class);

    interface SailingAnalyticsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopSailingAnalyticsView> {
    }

    public TabletAndDesktopSailingAnalyticsView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
