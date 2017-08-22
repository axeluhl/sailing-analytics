package com.sap.sailing.gwt.home.desktop.partials.anniversary;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView;

public class DesktopAnniversaries extends Composite implements AnniversariesView {

    private final FlowPanel panel;

    protected DesktopAnniversaries() {
        this.panel = new FlowPanel();
        panel.addStyleName(SharedResources.INSTANCE.mediaCss().grid());
        initWidget(panel);
    }

    @Override
    public void clearAnniversaries() {
        this.panel.clear();
    }

    @Override
    public void addAnniversary(String iconUrl, String teaser, String description) {
        this.panel.add(new AnniversaryItem(iconUrl, teaser, description));
    }

}
