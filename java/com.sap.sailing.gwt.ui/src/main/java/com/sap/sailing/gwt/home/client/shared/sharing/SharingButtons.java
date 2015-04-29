package com.sap.sailing.gwt.home.client.shared.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SharingButtons extends Composite {

    private static SharingButtonsUiBinder uiBinder = GWT.create(SharingButtonsUiBinder.class);

    interface SharingButtonsUiBinder extends UiBinder<Widget, SharingButtons> {
    }
    
    @UiField AnchorElement mail;
    @UiField AnchorElement twitter;
    @UiField AnchorElement facebook;

    public SharingButtons() {
        SharingButtonsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setUp(SharingMetadataProvider provider) {
        String shortText = provider.getShortText();
        String longText = provider.getLongText(Window.Location.getHref());
        
        UrlBuilder mailtoLink = new UrlBuilder().setProtocol("mailto").setParameter("subject", shortText).setParameter("body", longText);
        // URLBuilder encodes spaces in parameters using "+" instead of "%20". This causes problems in Mail programs that do not decode "+" as space.
        mail.setHref(mailtoLink.buildString().replace("+", "%20"));
        UrlBuilder twitterLink = new UrlBuilder().setProtocol("https").setHost("twitter.com").setPath("intent/tweet").setParameter("text", shortText).setParameter("url", Window.Location.getHref()).setParameter("short_url_length", "8");
        twitter.setHref(twitterLink.buildString());
        UrlBuilder facebookLink = new UrlBuilder().setProtocol("https").setHost("www.facebook.com").setPath("dialog/feed").setParameter("display", "page").setParameter("caption", shortText).setParameter("link", Window.Location.getHref()).setParameter("redirect_uri", Window.Location.getHref());
        facebook.setHref(facebookLink.buildString());
    }
}
