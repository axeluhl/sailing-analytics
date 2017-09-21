package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MoreLoginInformation extends Composite {

    private static MoreLoginInformationUiBinder uiBinder = GWT.create(MoreLoginInformationUiBinder.class);

    interface MoreLoginInformationUiBinder extends UiBinder<Widget, MoreLoginInformation> {
    }

    public MoreLoginInformation() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}
