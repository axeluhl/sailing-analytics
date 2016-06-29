package com.sap.sailing.gwt.home.shared.refresh;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

/**
 * Specialized {@link AutoAttachingRefreshManager} that initially shows a busy widget that is replayced with the actual
 * content when the first refresh is finished. In the case of an error while doing the first refresh, an error widget is
 * shown. When an error occurs on a further refresh, the old content simply isn't exchanged an a refresh is triggered as
 * described for {@link RefreshManager}.
 */
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
