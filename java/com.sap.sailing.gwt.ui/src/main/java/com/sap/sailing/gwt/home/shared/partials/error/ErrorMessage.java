package com.sap.sailing.gwt.home.shared.partials.error;

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
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class ErrorMessage extends Composite {

    interface ErrorUiBinder extends UiBinder<Widget, ErrorMessage> {
    }
    
    private static ErrorUiBinder uiBinder = GWT.create(ErrorUiBinder.class);

    @UiField DivElement errorMessage;
    @UiField DivElement errorMessageDetail;
    @UiField Anchor reloadPageAnchor;

    private Command reloadCommand;

    public ErrorMessage(String errorMessageDetail, Throwable errorReason, Command reloadCommand) {
        this(StringMessages.INSTANCE.errorMessageLoadingData(), errorMessageDetail, errorReason, reloadCommand);
    }
    
    public ErrorMessage(String errorMessage, String errorMessageDetail, Throwable errorReason, Command reloadCommand) {
        ErrorMessageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        this.reloadCommand = reloadCommand;
        
        if (errorMessage != null && !errorMessage.isEmpty()) {
            this.errorMessage.setInnerText(errorMessage);
        } else {
            this.errorMessage.setInnerText(StringMessages.INSTANCE.errorMessageLoadingData());
        }

        this.errorMessageDetail.setInnerText(errorMessageDetail);
        Notification.notify(errorMessageDetail, NotificationType.WARNING);
    }
    
    public void addReloadPageButtonStyleNames(String styleNames) {
        reloadPageAnchor.addStyleName(styleNames);
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
