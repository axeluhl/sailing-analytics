package com.sap.sailing.gwt.home.desktop.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LoginPopupContent extends Composite{

    private static LoginPopupContentUiBinder uiBinder = GWT.create(LoginPopupContentUiBinder.class);

    interface LoginPopupContentUiBinder extends UiBinder<Widget, LoginPopupContent> {
    }

    @UiField
    Anchor moreInfo;
    @UiField
    Anchor dismiss;

    public LoginPopupContent(final Runnable onDismiss, final Runnable onMoreInfo) {
        initWidget(uiBinder.createAndBindUi(this));
        dismiss.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                onDismiss.run();
            }
        });
        moreInfo.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                onMoreInfo.run();
            }
        });
    }
}
