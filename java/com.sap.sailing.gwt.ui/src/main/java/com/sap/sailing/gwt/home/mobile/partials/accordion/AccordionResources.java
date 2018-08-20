package com.sap.sailing.gwt.home.mobile.partials.accordion;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface AccordionResources extends ClientBundle {
    public static final AccordionResources INSTANCE = GWT.create(AccordionResources.class);

    @Source("Accordion.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String accordion();
        String accordioncollapsed();
        String accordion_item_header_arrow();
        String accordion_item();
        String accordion_item_header();
        String accordion_item_body();
        String accordion_item_header_title();
        String withHeader();
        String withFooter();
    }
}
