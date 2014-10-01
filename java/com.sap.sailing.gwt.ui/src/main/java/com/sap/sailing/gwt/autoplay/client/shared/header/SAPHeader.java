package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SAPHeader extends Composite {
    private static SAPHeaderUiBinder uiBinder = GWT.create(SAPHeaderUiBinder.class);

    interface SAPHeaderUiBinder extends UiBinder<Widget, SAPHeader> {
    }

    @UiField DivElement pageTitleDiv;
    
    public SAPHeader(String pageTitle) {
        SAPHeaderResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));
        
        pageTitleDiv.setInnerText(pageTitle);
    }
}
