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
        String accordion_trigger();
        String accordion_content();
        String solutions();
        String accordioncollapsed();
        String solutions_item_header_arrow();
        String solutions_item();
        String solutions_item_header();
        String solutions_item_body();
        String solutions_item_header_title();
        String withHeader();
        String withFooter();
    }
}
