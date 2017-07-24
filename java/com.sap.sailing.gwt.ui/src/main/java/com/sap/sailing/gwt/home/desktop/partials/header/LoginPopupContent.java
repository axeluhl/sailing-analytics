package com.sap.sailing.gwt.home.desktop.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class LoginPopupContent extends Composite{

    private static LoginPopupContentUiBinder uiBinder = GWT.create(LoginPopupContentUiBinder.class);

    interface LoginPopupContentUiBinder extends UiBinder<Widget, LoginPopupContent> {
    }

    @UiField
    Label txt;
    @UiField
    Anchor moreInfo;
    @UiField
    Anchor dismiss;

    public LoginPopupContent() {
        initWidget(uiBinder.createAndBindUi(this));
        
        txt.setText(StringMessages.INSTANCE.shortMoreInfoLogin());
        moreInfo.setText(StringMessages.INSTANCE.moreInfo());
        dismiss.setText(StringMessages.INSTANCE.dismiss());
    }

    public Anchor getDismiss() {
        return dismiss;
    }

    public Anchor getMoreInfo() {
        return moreInfo;
    }

}
