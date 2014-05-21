package com.sap.sailing.gwt.home.client.app;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ApplicationDesktopPresenter extends AbstractRootPagePresenter {

    @Inject
    public ApplicationDesktopPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
        super(eventBus, view, proxy);
    }

    @Override
    protected void onBind() {
        // TODO Auto-generated method stub
        super.onBind();
    }
}
