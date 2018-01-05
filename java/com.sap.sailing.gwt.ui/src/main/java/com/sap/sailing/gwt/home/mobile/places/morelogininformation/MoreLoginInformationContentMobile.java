package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.home.mobile.partials.accordion.AccordionItem;

public class MoreLoginInformationContentMobile extends AccordionItem {

    @UiConstructor
    public MoreLoginInformationContentMobile(String title, String content, ImageResource image) {
        super(title, image, title, true);
        addContent(new Label(content));
    }

}
