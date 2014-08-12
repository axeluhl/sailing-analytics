package com.sap.sailing.gwt.home.client.place.error;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.error.ErrorMessage;

public class TabletAndDesktopErrorView extends Composite implements ErrorView {
    private static ErrorViewUiBinder uiBinder = GWT.create(ErrorViewUiBinder.class);

    interface ErrorViewUiBinder extends UiBinder<Widget, TabletAndDesktopErrorView> {
    }

    @UiField(provided=true) ErrorMessage errorMessage;
    
    public TabletAndDesktopErrorView(String errorMessageText, Throwable errorReason) {
        errorMessage = new ErrorMessage(errorMessageText, errorReason);
        
        initWidget(uiBinder.createAndBindUi(this));
    }
}
