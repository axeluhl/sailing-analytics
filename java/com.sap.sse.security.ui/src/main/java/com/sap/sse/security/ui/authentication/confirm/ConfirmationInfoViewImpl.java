package com.sap.sse.security.ui.authentication.confirm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.authentication.AuthenticationSharedResources;

public class ConfirmationInfoViewImpl extends Composite implements ConfirmationInfoView {
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, ConfirmationInfoViewImpl> {
    }

    @UiField protected Label messageUi;
    @UiField(provided = true) final AuthenticationSharedResources res;

    public ConfirmationInfoViewImpl(AuthenticationSharedResources resources) {
        this.res = resources;
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setMessage(String message) {
        messageUi.setText(message);
    }

}
