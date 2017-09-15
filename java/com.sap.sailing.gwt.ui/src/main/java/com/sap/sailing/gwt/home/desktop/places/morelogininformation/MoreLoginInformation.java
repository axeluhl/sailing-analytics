package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class MoreLoginInformation extends Composite {

    private static MoreLoginInformationUiBinder uiBinder = GWT.create(MoreLoginInformationUiBinder.class);

    interface MoreLoginInformationUiBinder extends UiBinder<Widget, MoreLoginInformation> {
    }
    
    @UiField
    FlowPanel contentHolderUi;

    public MoreLoginInformation() {
        initWidget(uiBinder.createAndBindUi(this));
        contentHolderUi.add(new MoreLoginInformationContent("TODO: settings", "TODO settings benefits", null, true));
        contentHolderUi.add(new MoreLoginInformationContent("TODO: Simulator", "TODO about simulator", null, true));
        contentHolderUi.add(new MoreLoginInformationContent("TODO: Notifications", "TODO about notifications", null, true));
    }

}
