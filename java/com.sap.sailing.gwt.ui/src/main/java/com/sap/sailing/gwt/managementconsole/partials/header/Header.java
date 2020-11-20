package com.sap.sailing.gwt.managementconsole.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Header extends Composite {

    interface HeaderUiBinder extends UiBinder<Widget, Header> {
    }

    private static HeaderUiBinder uiBinder = GWT.create(HeaderUiBinder.class);

    @UiField
    HeaderResources local_res;

    public Header() {
        initWidget(uiBinder.createAndBindUi(this));
        local_res.style().ensureInjected();
    }
}
