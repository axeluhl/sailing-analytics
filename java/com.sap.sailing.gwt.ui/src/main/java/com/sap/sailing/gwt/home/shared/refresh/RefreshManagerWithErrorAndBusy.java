package com.sap.sailing.gwt.home.shared.refresh;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public class RefreshManagerWithErrorAndBusy extends AutoAttachingRefreshManager {
    private final AcceptsOneWidget container;

    private final ErrorAndBusyClientFactory errorAndBusyViewFactory;
    
    private boolean hadSuccess = false;
    private boolean hadFailure = false;
    
    public RefreshManagerWithErrorAndBusy(Widget content, AcceptsOneWidget container,
            SailingDispatchSystem actionExecutor, ErrorAndBusyClientFactory errorAndBusyViewFactory) {
        super(content, container, actionExecutor);
        this.container = container;
        this.errorAndBusyViewFactory = errorAndBusyViewFactory;
        container.setWidget(errorAndBusyViewFactory.createBusyView());
    }
    
    @Override
    protected void onFailedUpdate(Throwable errorCause) {
        super.onFailedUpdate(errorCause);
        if(!hadSuccess && !hadFailure) {
            container.setWidget(errorAndBusyViewFactory.createErrorView(StringMessages.INSTANCE.errorLoadingDataWithTryAgain(), errorCause));
        }
        hadFailure = true;
    }
    
    @Override
    protected void onSuccessfulUpdate() {
        super.onSuccessfulUpdate();
        hadSuccess = true;
    }
}
