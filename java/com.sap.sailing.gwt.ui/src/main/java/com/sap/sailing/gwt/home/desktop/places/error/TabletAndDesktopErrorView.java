package com.sap.sailing.gwt.home.desktop.places.error;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.shared.partials.error.ErrorMessage;
import com.sap.sse.gwt.client.mvp.ErrorView;

public class TabletAndDesktopErrorView extends Composite implements ErrorView {
    private static ErrorViewUiBinder uiBinder = GWT.create(ErrorViewUiBinder.class);

    interface ErrorViewUiBinder extends UiBinder<Widget, TabletAndDesktopErrorView> {
    }

    @UiField(provided=true) ErrorMessage errorMessageUi;
    
    public TabletAndDesktopErrorView(String errorMessageTextDetail, Throwable errorReason, Command reloadCommand) {
        this(new ErrorMessage(errorMessageTextDetail, errorReason, reloadCommand));
    }

    public TabletAndDesktopErrorView(String customMessageText, String errorMessageTextDetail, Throwable errorReason,
            Command reloadCommand) {
        this(new ErrorMessage(customMessageText, errorMessageTextDetail, errorReason, reloadCommand));
    }
    
    private TabletAndDesktopErrorView(ErrorMessage errorMessage) {
        errorMessageUi = errorMessage;
        initWidget(uiBinder.createAndBindUi(this));
        errorMessageUi.addReloadPageButtonStyleNames(SharedResources.INSTANCE.mainCss().button());
    }
}
