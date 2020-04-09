package com.sap.sailing.gwt.home.mobile.partials.socialfooter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.shared.ClientConfiguration;

public class SocialFooter extends Composite {

    interface SocialFooterUiBinder extends UiBinder<Widget, SocialFooter> {
    }
    
    private static SocialFooterUiBinder uiBinder = GWT.create(SocialFooterUiBinder.class);
    
    @UiField HTMLPanel htmlPanel;

    public SocialFooter() {
        SocialFooterResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        if (!ClientConfiguration.getInstance().isBrandingActive()) {
            htmlPanel.getElement().getStyle().setDisplay(Display.NONE);
        }
    }

}
