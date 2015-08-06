package com.sap.sailing.gwt.home.mobile.partials.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Solutions extends Composite {

    interface MyUiBinder extends UiBinder<Widget, Solutions> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    public Solutions() {
        SolutionsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

}
