package com.sap.sailing.gwt.home.mobile.partials.eventdescription;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class EventDescription extends Composite {
    
    public EventDescription(String description) {
        final MobileSection section = new MobileSection();
        SectionHeaderContent header = new SectionHeaderContent();
        header.setSectionTitle(StringMessages.INSTANCE.description());
        section.addHeader(header);
        final Label content = new Label(description);
        content.getElement().getStyle().setPaddingTop(0.5, Unit.EM);
        content.getElement().getStyle().setPaddingBottom(0.75, Unit.EM);
        section.addContent(content);
        initWidget(section);
    }

}
