package com.sap.sailing.gwt.managementconsole.places.showcase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ShowcaseView extends Composite {

    interface ShowcaseViewUiBinder extends UiBinder<Widget, ShowcaseView> {
    }

    private static ShowcaseViewUiBinder uiBinder = GWT.create(ShowcaseViewUiBinder.class);

    public ShowcaseView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

}