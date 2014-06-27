package com.sap.sailing.gwt.home.client.shared.sponsoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Sponsoring extends Composite {

    interface SolutionsUiBinder extends UiBinder<Widget, Sponsoring> {
    }
    
    private static SolutionsUiBinder uiBinder = GWT.create(SolutionsUiBinder.class);

    public Sponsoring() {
        SponsoringResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
