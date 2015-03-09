package com.sap.sailing.gwt.home.client.shared.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.http.client.URL;
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
        String lorem = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. ";
        
        // URLBuilder doesn't work for the Mail link as spaces in parameters are encodes using "+" instead of "%20". This causes problems in Mail programs that do not decode "+" as space.
        String mailLink = "mailto:?subject="+ URL.encode(provider.getSharingTitle())+"&body=" + URL.encode(lorem + Window.Location.getHref());
        mail.setHref(mailLink);
        UrlBuilder twitterLink = new UrlBuilder().setProtocol("https").setHost("twitter.com").setPath("intent/tweet").setParameter("text", provider.getSharingTitle()).setParameter("url", Window.Location.getHref()).setParameter("short_url_length", "8");
        twitter.setHref(twitterLink.buildString());
        UrlBuilder facebookLink = new UrlBuilder().setProtocol("https").setHost("www.facebook.com").setPath("dialog/feed").setParameter("app_id", "145634995501895").setParameter("display", "page").setParameter("caption", provider.getSharingTitle()).setParameter("link", Window.Location.getHref()).setParameter("redirect_uri", Window.Location.getHref());
        facebook.setHref(facebookLink.buildString());
    }
}
