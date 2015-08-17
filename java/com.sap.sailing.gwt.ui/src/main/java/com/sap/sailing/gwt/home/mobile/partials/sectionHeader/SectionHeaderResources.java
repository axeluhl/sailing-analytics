package com.sap.sailing.gwt.home.mobile.partials.sectionHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SectionHeaderResources extends ClientBundle {
    public static final SectionHeaderResources INSTANCE = GWT.create(SectionHeaderResources.class);

    @Source("SectionHeader.gss")
    LocalCss css();

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
        String sectionheader_item_adjust_title();
        String accordion();
        String collapsed();
    }
}
