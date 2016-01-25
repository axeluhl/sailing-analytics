package com.sap.sailing.gwt.home.shared.usermanagement.confirm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.client.usermanagement.UserManagementSharedResources;

public class ConfirmationInfoViewImpl extends Composite implements ConfirmationInfoView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, ConfirmationInfoViewImpl> {
    }

    @UiField protected DivElement messageTitleUi;
    @UiField protected Element messageUi;
    @UiField(provided = true) final UserManagementSharedResources res;

    public ConfirmationInfoViewImpl(UserManagementSharedResources resources, String messageTitle) {
        this.res = resources;
        initWidget(uiBinder.createAndBindUi(this));
        messageTitleUi.setInnerText(messageTitle);
    }
    
    @Override
    public void setMessage(String message) {
        messageUi.setInnerText(message);
    }

}
