package com.sap.sailing.gwt.home.shared.usermanagement.confirm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.authentication.UserManagementSharedResources;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoView;

public class ConfirmationInfoViewImpl extends Composite implements ConfirmationInfoView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, ConfirmationInfoViewImpl> {
    }

    @UiField protected Label messageUi;
    @UiField(provided = true) final UserManagementSharedResources res;

    public ConfirmationInfoViewImpl(UserManagementSharedResources resources) {
        this.res = resources;
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setMessage(String message) {
        messageUi.setText(message);
    }

}
