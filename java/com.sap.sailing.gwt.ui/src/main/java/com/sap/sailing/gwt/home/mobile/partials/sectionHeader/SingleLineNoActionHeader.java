package com.sap.sailing.gwt.home.mobile.partials.sectionHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class SingleLineNoActionHeader extends Widget {
    
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<DivElement, SingleLineNoActionHeader> {
    }

    @UiField
    Element titleUi;
    
    public SingleLineNoActionHeader() {
        SectionHeaderResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
    }
    
    public void setSectionTitle(String sectionHeaderTitle) {
        titleUi.setInnerText(sectionHeaderTitle);
    }


}
