package com.sap.sailing.gwt.home.client.place.sponsoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TabletAndDesktopSponsoringView extends Composite implements SponsoringView {
    private static SponsoringPageViewUiBinder uiBinder = GWT.create(SponsoringPageViewUiBinder.class);

    interface SponsoringPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopSponsoringView> {
    }

    public TabletAndDesktopSponsoringView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
