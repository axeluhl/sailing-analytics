package com.sap.sailing.gwt.home.client.app;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ApplicationTabletPresenter extends AbstractRootPagePresenter {

    @Inject
    public ApplicationTabletPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
        super(eventBus, view, proxy);
    }
}
