package com.sap.sailing.gwt.home.mobile.places.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SolutionsViewImpl extends Composite implements SolutionsView {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Widget, SolutionsViewImpl> {
    }


    public SolutionsViewImpl(Presenter presenter) {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
