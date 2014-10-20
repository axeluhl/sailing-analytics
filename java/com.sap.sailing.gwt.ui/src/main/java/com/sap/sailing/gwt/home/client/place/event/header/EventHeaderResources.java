package com.sap.sailing.gwt.home.client.place.event.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource;

public interface EventHeaderResources extends ClientBundle {
    public static final EventHeaderResources INSTANCE = GWT.create(EventHeaderResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/header/EventHeader.css")
    LocalCss css();

    public interface MinWidth50emCss extends LocalCss {
    }

    @Source("com/sap/sailing/gwt/home/client/place/event/header/EventHeaderLarge.css")
    MinWidth50emCss largeCss();

    @Shared
    public interface LocalCss extends CssResource {
        String dropdown();
        String dropdown_head();
        String dropdown_head_title();
        String dropdown_head_titlebutton();
        String dropdown_content();
        String dropdown_content_link();
        String dropdown_content_link_title();
        String dropdown_content_link_subtitle();
        String leaderboardquickaccess();
        String eventheaderwrapper();
        String eventheader();
        String eventnavigationnormal();
        String eventnavigationcompact();
        String eventnavigationcompactfloating();
        String eventheader_intro();
        String eventheader_intro_description();
        String eventheader_intro_logo();
        String eventheader_intro_logo_image();
        String eventheader_intro_name();
        String eventheader_intro_details();
        String eventheader_intro_details_item();
        String eventheader_intro_details_itemlink();
        String eventheader_sharing();
        String eventheader_sharing_item();
        String eventheader_sharing_itememail();
        String eventheader_sharing_itemtwitter();
        String eventheader_sharing_itemfacebook();
        String locationicon();
        String eventnavigation();
        String eventnavigation_link();
        String eventnavigation_linkactive();
        String eventnavigationcompact_logo();
        String eventnavigationcompactfloating_logo();
        String eventnavigationfixed();
    }

    @Source("com/sap/sailing/gwt/home/images/default_event_logo.jpg")
    ImageResource defaultEventLogoImage();

}
