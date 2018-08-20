package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.AbstractMoreLoginInformation;

/**
 * Desktop page that shows the benefits of logging in on sapsailing.com.
 */
public class MoreLoginInformationDesktop extends AbstractMoreLoginInformation {

    private static MoreLoginInformationUiBinder uiBinder = GWT.create(MoreLoginInformationUiBinder.class);

    interface MoreLoginInformationUiBinder extends UiBinder<Widget, AbstractMoreLoginInformation> {
    }

    public MoreLoginInformationDesktop(Runnable registerCallback) {
        super(uiBinder, registerCallback);
    }

}
