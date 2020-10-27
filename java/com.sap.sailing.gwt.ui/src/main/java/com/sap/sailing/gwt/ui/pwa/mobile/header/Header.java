package com.sap.sailing.gwt.ui.pwa.mobile.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class Header extends Composite {
    
    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }

    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    @UiField
    HTMLPanel headerPanel;
    
    public Header() {
        HeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
       
        Label label = new Label("Mobile View");
        headerPanel.add(label); 
    }

}
