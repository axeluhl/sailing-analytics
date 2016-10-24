package com.sap.sailing.gwt.home.mobile.places.error;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.partials.error.ErrorMessage;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class ErrorViewImpl extends Composite implements ErrorView {

    private static ErrorViewUiBinder uiBinder = GWT.create(ErrorViewUiBinder.class);
    
    interface ErrorViewUiBinder extends UiBinder<Widget, ErrorViewImpl> {
    }
    
    @UiField(provided = true) ErrorMessage errorMessageUi;
    
    public ErrorViewImpl(String errorMessageDetail, Throwable errorReason, Command reloadCommand) {
        this(new ErrorMessage(errorMessageDetail, errorReason, reloadCommand));
    }
    
    public ErrorViewImpl(String errorMessage, String errorMessageDetail, Throwable errorReason, Command reloadCommand) {
        this(new ErrorMessage(errorMessage, errorMessageDetail, errorReason, reloadCommand));
    }
    
    private ErrorViewImpl(ErrorMessage errorMessage) {
        errorMessageUi = errorMessage;
        initWidget(uiBinder.createAndBindUi(this));
        errorMessageUi.addReloadPageButtonStyleNames(SharedResources.INSTANCE.mediaCss().small12());
    }

}
 