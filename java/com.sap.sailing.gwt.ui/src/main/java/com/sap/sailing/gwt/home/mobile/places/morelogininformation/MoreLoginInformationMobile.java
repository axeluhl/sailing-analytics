package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Mobile page that shows the benefits of logging in on sapsailing.com.
 */
public class MoreLoginInformationMobile extends Composite {

    private static MoreLoginInformationUiBinder uiBinder = GWT.create(MoreLoginInformationUiBinder.class);

    interface MoreLoginInformationUiBinder extends UiBinder<Widget, MoreLoginInformationMobile> {
    }

    public MoreLoginInformationMobile() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
