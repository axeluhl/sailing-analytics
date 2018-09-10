package com.sap.sailing.gwt.home.desktop.partials.desktopaccordion;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface DesktopAccordionResources extends ClientBundle {

    public static final DesktopAccordionResources INSTANCE = GWT.create(DesktopAccordionResources.class);

    @Source("DesktopAccordionResources.gss")
    SailorProfilesCss css();

    public interface SailorProfilesCss extends CssResource {

        String accordionTitle();

        String accordionHeader();

        String accordionHeaderRight();

        String accordionHeaderRightArrow();

        String accordionArrowImage();

        String accordionCollapsed();

        String accordion();

        String accordionContentPanel();
    }
}
