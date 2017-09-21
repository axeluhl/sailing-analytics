package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import com.github.gwtbootstrap.client.ui.Image;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;

public class MoreLoginInformationContentMobile extends Composite {
    ImageElement imageUi;

    @UiConstructor
    public MoreLoginInformationContentMobile(String title, String content, ImageResource image, boolean imageOnLeft) {
        MobileSection section = new MobileSection();
        
        SectionHeaderContent header = new SectionHeaderContent();
        header.setSectionTitle(title);
        
        section.addHeader(header);
        Image img = new Image(image);
        section.addContent(img);
        section.addContent(new Label(content));
        
        initWidget(section);
    }

}
