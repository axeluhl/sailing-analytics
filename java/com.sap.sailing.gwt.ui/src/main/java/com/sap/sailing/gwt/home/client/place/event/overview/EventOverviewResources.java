package com.sap.sailing.gwt.home.client.place.event.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface EventOverviewResources extends ClientBundle {
    public static final EventOverviewResources INSTANCE = GWT.create(EventOverviewResources.class);

    @Source("com/sap/sailing/gwt/home/client/place/event/overview/EventOverview.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String latestraceresults();
        String latestraceresults_refresh();
        String latestraceresults_headline();
        String latestraceresults_link();
        String latestraceresults_link_title();
        String latestraceresults_detail();
        String latestraceresults_detail_wrapper();
        String latestraceresults_detail_wrapper_medal();
        String last();
        String latestraceresults_detail_wrapper_thumb();
        String latestraceresults_detail_wrapper_more();
        String latestraceresults_detail_title();
        String latestraceresults_link_raceCount();
        String latestraceresults_detail_raceCount();
        String latestraceresults_link_datetime();
        String latestraceresults_detail_datetime();
        String columns();
        String latestraceresults_detail_wrapperimage();
        String mediateaser();
        String mediateaser_title();
        String mediateaser_title_link();
        String mediateaser_title_value();
        String mediateaser_photo();
        String mediateaser_photomain();
        String mediateaser_photothumb();
        String mediateaser_video();
        String playablevideo_buttonplay();
        String box();
        String boxlink();
        String box_header();
        String box_header_title();
        String box_content();
        String box_contentlink();
        String upcomingraceshort();
        String upcomingraceshort_name();
        String upcomingraceshort_date();
        String upcomingraceshort_date_today();
        String upcomingraceshort_date_day();
        String upcomingraceshort_date_month();
        String upcomingraceshort_subtitle();
        String upcomingraceshort_details();
        String upcomingraceshort_countdown();
        String weathershort();
        String grid();
        String weathershort_title();
        String weathershort_data();
        String weathershort_datatemperature();
        String weathershort_datawindspeed();
        String weathershort_datawinddirection();
        String newsupdate();
        String newsupdate_date();
        String dropdown();
        String dropdown_content();
        String dropdown_head();
        String dropdown_head_title();
        String dropdown_head_titlebutton();
        String dropdown_content_link();
        String dropdown_content_link_title();
        String dropdown_content_link_subtitle();
        String eventquickfinder();
        String eventlivebox();
        String eventlivebox_video_container();
        String eventlivebox_video_container_captionlayer();
        String eventlivebox_video_container_caption();
        String label();
        String eventlivebox_linklist();
        String eventlivebox_linklist_link();
        String eventlivebox_linklist_link_title();
        String eventlivebox_linklist_link_icon();
        String eventlivebox_linklist_link_description();
        String eventoverview();
    }
}
