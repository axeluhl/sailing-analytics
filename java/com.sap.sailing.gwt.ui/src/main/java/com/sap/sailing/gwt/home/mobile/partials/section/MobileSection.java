package com.sap.sailing.gwt.home.mobile.partials.section;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderResources;

public class MobileSection extends Composite {
    
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, MobileSection> {
    }

    @UiField
    SimplePanel sectionHeaderUi;
    @UiField
    SimplePanel sectionContentUi;
    
    public MobileSection() {
        MobileSectionResources.INSTANCE.css().ensureInjected();
        SectionHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiChild
    public void addHeader(Widget theHeaderWidget) {
        sectionHeaderUi.setWidget(theHeaderWidget);
    }

    @UiChild
    public void addContent(Widget theContentWidget) {
        sectionContentUi.setWidget(theContentWidget);
    }
    
}
