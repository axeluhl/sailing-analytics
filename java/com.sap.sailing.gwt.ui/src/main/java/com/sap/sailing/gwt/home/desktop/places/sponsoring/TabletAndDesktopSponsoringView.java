package com.sap.sailing.gwt.home.desktop.places.sponsoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.sponsoring.Sponsoring;

public class TabletAndDesktopSponsoringView extends Composite implements SponsoringView {
    private static SponsoringPageViewUiBinder uiBinder = GWT.create(SponsoringPageViewUiBinder.class);

    interface SponsoringPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopSponsoringView> {
    }

    @UiField Sponsoring sponsoring;

    public TabletAndDesktopSponsoringView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
