package com.sap.sailing.gwt.home.mobile.partials.sectionHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;

public interface SectionHeaderResources extends SharedHomeResources {
    public static final SectionHeaderResources INSTANCE = GWT.create(SectionHeaderResources.class);

    @Source("SectionHeader.gss")
    LocalCss css();
    
    @Source("filter.svg")
    @MimeType("image/svg+xml")
    DataResource filter();

    public interface LocalCss extends CssResource {
        String sectionheader();
        String sectionheaderstandalone();
        String sectionheader_item();
        String sectionheader_itemdoublerow();
        String sectionheader_itemright();
        String sectionheader_item_image();
        String sectionheader_item_image_seperator();
        String sectionheader_item_title();
        String sectionheader_item_label();
        String sectionheader_item_subtitle();
        String sectionheader_item_infotext();
        String sectionheader_item_arrow();
        String sectionheader_item_adjust_title_left();
        String sectionheader_item_adjust_title_right();
        String sectionheader_item_select();
        String accordion();
        String collapsed();
    }
}
