package com.sap.sse.gwt.client.controls.dropdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sse.gwt.common.CommonIcons;

public interface DropdownResources extends CommonIcons {
    static final DropdownResources INSTANCE = GWT.create(DropdownResources.class);

    @Source("Dropdown.gss")
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
