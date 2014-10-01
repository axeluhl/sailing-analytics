package com.sap.sailing.gwt.home.client.shared.socialfooter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SocialFooterResources extends ClientBundle {
    public static final SocialFooterResources INSTANCE = GWT.create(SocialFooterResources.class);

    @Source("com/sap/sailing/gwt/home/client/shared/socialfooter/SocialFooter.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String socialfooter();
        String mainsection_header();
        String mainsection_header_title();
        String socialfooter_button();
        String socialfooter_buttontwitter();
        String socialfooter_buttonfacebook();
        String socialfooter_button_text();
        String socialfooter_button_widget();
    }
}
