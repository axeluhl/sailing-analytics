package com.sap.sailing.gwt.home.mobile.partials.socialfooter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SocialFooter extends Composite {

    interface SocialFooterUiBinder extends UiBinder<Widget, SocialFooter> {
    }
    
    private static SocialFooterUiBinder uiBinder = GWT.create(SocialFooterUiBinder.class);

    public SocialFooter() {
        SocialFooterResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
