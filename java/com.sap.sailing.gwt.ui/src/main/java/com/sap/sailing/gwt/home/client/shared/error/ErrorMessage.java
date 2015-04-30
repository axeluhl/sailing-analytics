package com.sap.sailing.gwt.home.client.shared.error;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public class ErrorMessage extends Composite {

    interface ErrorUiBinder extends UiBinder<Widget, ErrorMessage> {
    }
    
    private static ErrorUiBinder uiBinder = GWT.create(ErrorUiBinder.class);

    @UiField DivElement errorMessage;
    @UiField DivElement errorMessageDetail;
    @UiField Anchor reloadPageAnchor;

    private Command reloadCommand;

    public ErrorMessage(String errorMessageDetail, Throwable errorReason, Command reloadCommand) {
        this(TextMessages.INSTANCE.errorMessageLoadingData(), errorMessageDetail, errorReason, reloadCommand);

    }
    
    public ErrorMessage(String errorMessage, String errorMessageDetail, Throwable errorReason, Command reloadCommand) {
        ErrorMessageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        this.reloadCommand = reloadCommand;
        this.setHeight(Window.getClientHeight() + "px");
        
        if (errorMessage != null && !errorMessage.isEmpty()) {
            this.errorMessage.setInnerText(errorMessage);
        } else {
            this.errorMessage.setInnerText(TextMessages.INSTANCE.errorMessageLoadingData());
        }

        this.errorMessageDetail.setInnerText(errorMessageDetail);
        Window.setStatus(errorMessageDetail);
    }
    
    @UiHandler("reloadPageAnchor")
    void reloadPage(ClickEvent e) {
        if (reloadCommand != null) {
            reloadCommand.execute();
        } else {
            Window.Location.reload();
        }

    }
}
