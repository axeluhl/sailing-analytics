package com.sap.sailing.gwt.home.mobile.partials.seriesheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SeriesHeaderResources extends ClientBundle {
    public static final SeriesHeaderResources INSTANCE = GWT.create(SeriesHeaderResources.class);

    @Source("SeriesHeader.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String eventheader();
        String eventheader_info_subtitle();

        String eventheader_info();
        String eventheader_info_title();
        String eventheader_info_title_text();
        String eventheader_info_title_text_label();
        String eventheader_info_subtitlereduced();
        String eventheader_info_subtitle_logo();
        String eventheader_info_subtitle_info();
        String eventheader_info_subtitle_info_date();
        String eventheader_info_subtitle_info_location();
    }
}
