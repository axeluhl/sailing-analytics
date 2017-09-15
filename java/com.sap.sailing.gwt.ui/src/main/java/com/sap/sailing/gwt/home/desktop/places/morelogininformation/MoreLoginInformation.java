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
        contentHolderUi.add(new MoreLoginInformationContent("TODO: settings", "TODO Many parts of the user interface are enabled to provide user-changeable settings. While the basic functionality is available to everyone, settings of authenticated users are stored in the user profile and are available on any device just by logging in.", MoreLoginInformationResources.INSTANCE.settings().getSafeUri(), true));
        contentHolderUi.add(new MoreLoginInformationContent("TODO: Simulator", "TODO The simulator enables sailors to simulate strategies for legs based on confugurable wind conditions. The simulator is based on all the historical data available in SAP Sailing Analytics to provide simulations on the accurate behaviour of specific boat classes.",
                MoreLoginInformationResources.INSTANCE.simulator().getSafeUri(), false));
        contentHolderUi.add(new MoreLoginInformationContent("TODO: Notifications", "TODO User notifications are an easy way to keep you up to date about your favourite competitor/team or boat class.",
                MoreLoginInformationResources.INSTANCE.notifications().getSafeUri(), true));
    }

}
