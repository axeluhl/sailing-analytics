package com.sap.sailing.gwt.home.client.place.error;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.error.ErrorMessage;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class TabletAndDesktopErrorView extends Composite implements ErrorView {
    private static ErrorViewUiBinder uiBinder = GWT.create(ErrorViewUiBinder.class);

    interface ErrorViewUiBinder extends UiBinder<Widget, TabletAndDesktopErrorView> {
    }

    @UiField(provided=true) ErrorMessage errorMessage;
    
    public TabletAndDesktopErrorView(String errorMessageTextDetail, Throwable errorReason, Command reloadCommand) {
        errorMessage = new ErrorMessage(errorMessageTextDetail, errorReason, reloadCommand);
        initWidget(uiBinder.createAndBindUi(this));
    }

    public TabletAndDesktopErrorView(String customMessageText, String errorMessageTextDetail, Throwable errorReason,
            Command reloadCommand) {
        errorMessage = new ErrorMessage(customMessageText, errorMessageTextDetail, errorReason, reloadCommand);
        initWidget(uiBinder.createAndBindUi(this));
    }
}
