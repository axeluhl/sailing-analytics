package com.sap.sailing.gwt.ui.pwa.mobile.header;

import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Header extends Composite {
    
    private static final Logger LOG = Logger.getLogger(Header.class.getName());
    
    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }

    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    public Header() {
        HeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
