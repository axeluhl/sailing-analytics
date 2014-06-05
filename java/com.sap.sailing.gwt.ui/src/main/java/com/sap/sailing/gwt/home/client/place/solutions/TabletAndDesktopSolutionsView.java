package com.sap.sailing.gwt.home.client.place.solutions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class TabletAndDesktopSolutionsView extends Composite implements SolutionsView {
    private static SolutionsPageViewUiBinder uiBinder = GWT.create(SolutionsPageViewUiBinder.class);

    interface SolutionsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopSolutionsView> {
    }

    public TabletAndDesktopSolutionsView() {
        super();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
