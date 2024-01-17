package com.sap.sailing.gwt.home.desktop.partials.eventheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.desktop.resources.SharedDesktopResources;

public interface DropdownItemResources extends SharedDesktopResources {
    public static final DropdownItemResources INSTANCE = GWT.create(DropdownItemResources.class);

    @Source("DropdownItem.gss")
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
        String dropdown_content_linkactive();
    }
}
