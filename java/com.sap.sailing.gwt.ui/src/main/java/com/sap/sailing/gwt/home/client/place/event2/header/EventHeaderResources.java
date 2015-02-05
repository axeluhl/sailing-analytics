package com.sap.sailing.gwt.home.client.place.event2.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface EventHeaderResources extends ClientBundle {
    public static final EventHeaderResources INSTANCE = GWT.create(EventHeaderResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event2/header/EventHeader.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String jsdropdown();
        String jsdropdown_head();
        String jsdropdown_content();
        String dropdown();
        String dropdown_head();
        String dropdown_head_title();
        String dropdown_head_title_button();
        String dropdown_content();
        String jsdropdownactive();
        String dropdown_content_link();
        String dropdown_content_link_title();
        String dropdown_content_link_subtitle();
        String dropdownregattas();
        String dropdownregattas_head();
        String dropdownregattas_head_title();
        String dropdownregattas_head_title_button();
        String dropdownregattas_content();
        String dropdownregattas_content_link();
        String dropdownregattas_content_linkactive();
        String dropdownregattas_content_link_title();
        String leaderboardquickaccess();
        String eventheader();
        String eventheader_breadcrumb();
        String eventheader_breadcrumbback();
        String eventheader_intro();
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
        String eventheader_status();
        String eventheader_status_title();
        String eventheader_status_body();
        String locationicon();
        String eventnavigation();
        String eventnavigationnormal();
        String eventnavigation_dropdown();
        String eventnavigation_dropdown_toggle();
        String eventnavigation_dropdown_toggle_icon();
        String eventnavigation_dropdown_container();
        String eventnavigation_dropdown_containerhidden();
        String navbar_button();
        String navbar_buttonhidden();
    }

    @Source("com/sap/sailing/gwt/home/images/default_event_logo.jpg")
    ImageResource defaultEventLogoImage();

}
