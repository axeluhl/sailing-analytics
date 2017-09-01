package com.sap.sailing.gwt.home.desktop.partials.anniversary;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView;

/**
 * {@link AnniversariesView} implementation for desktop.
 */
public class DesktopAnniversaries extends Composite implements AnniversariesView {

    private final FlowPanel panel;

    protected DesktopAnniversaries() {
        initWidget(this.panel = new FlowPanel());
    }

    @Override
    public void clearAnniversaries() {
        this.panel.clear();
    }

    @Override
    public void addCountdown(int countdown, String teaser, String description) {
        this.panel.add(new AnniversaryItem(countdown, teaser, description));
    }

    @Override
    public void addAnnouncement(String iconUrl, int target, String teaser, String description, String linkUrl) {
        this.panel.add(new AnniversaryItem(iconUrl, target, teaser, description, linkUrl));
    }

}
