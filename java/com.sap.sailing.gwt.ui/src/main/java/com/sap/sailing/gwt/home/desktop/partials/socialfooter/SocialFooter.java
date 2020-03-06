package com.sap.sailing.gwt.home.desktop.partials.socialfooter;

import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.sap.sse.gwt.shared.DebugConstants.DEBUG_ID_ATTRIBUTE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.shared.Branding;

public class SocialFooter extends Composite {

    interface SocialFooterUiBinder extends UiBinder<Widget, SocialFooter> {
    }

    private static SocialFooterUiBinder uiBinder = GWT.create(SocialFooterUiBinder.class);

    @UiField
    HTMLPanel htmlPanel;

    public SocialFooter() {
        SocialFooterResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        if (!Branding.getInstance().isActive()) {
            htmlPanel.getElement().getStyle().setDisplay(NONE);
        }
        htmlPanel.getElement().setAttribute(DEBUG_ID_ATTRIBUTE, "socialFooter");
    }

}
