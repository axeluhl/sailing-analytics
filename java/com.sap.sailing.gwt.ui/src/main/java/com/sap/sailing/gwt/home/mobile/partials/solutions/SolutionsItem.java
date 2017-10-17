package com.sap.sailing.gwt.home.mobile.partials.solutions;

import com.google.gwt.uibinder.client.UiConstructor;
import com.sap.sailing.gwt.home.mobile.partials.accordion.AccordionItem;

public class SolutionsItem extends AccordionItem {

    @UiConstructor
    public SolutionsItem(String title, String imageUrl, boolean showInitial) {
        super(title, imageUrl, title, showInitial);
    }

}
