package com.sap.sailing.gwt.home.client;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

/**
 * This is the top-level presenter of the hierarchy. Other presenters reveal themselves within this presenter.
 * <p />
 * The goal of this sample is to show how to use nested presenters. These can be useful to decouple multiple presenters
 * that need to be displayed on the screen simultaneously.
 */
public class RootPagePresenter extends Presenter<RootPagePresenter.MyView, RootPagePresenter.MyProxy> {
    /**
     * {@link RootPagePresenter}'s proxy.
     */
    @ProxyStandard
    public interface MyProxy extends Proxy<RootPagePresenter> {
    }

    /**
     * {@link RootPagePresenter}'s view.
     */
    public interface MyView extends View {
        void showLoading(boolean visibile);
    }

    /**
     * Use this in leaf presenters, inside their {@link #revealInParent} method.
     */
    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    @Inject
    public RootPagePresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
        super(eventBus, view, proxy);
    }

    @Override
    protected void revealInParent() {
        RevealRootContentEvent.fire(this, this);
    }

    /**
     * We display a short lock message whenever navigation is in progress.
     * 
     * @param event
     *            The {@link LockInteractionEvent}.
     */
    @ProxyEvent
    public void onLockInteraction(LockInteractionEvent event) {
        getView().showLoading(event.shouldLock());
    }
}
