package com.sap.sailing.gwt.home.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class StartPagePresenter extends Presenter<StartPagePresenter.MyView, StartPagePresenter.MyProxy> {
    private final PlaceManager placeManager;
    
    public interface MyView extends View {
        HasClickHandlers getGotoEventsPageButton();

        TextBox getTestBox();
    }

    /**
     * Child presenters can fire a RevealContentEvent with TYPE_SetMainContent to set themselves
     * as children of this presenter.
     */
    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    @ProxyCodeSplit
    @NameToken(PageNameTokens.startPage)
    public interface MyProxy extends ProxyPlace<StartPagePresenter> {
    }

    @Inject
    public StartPagePresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager) {
        super(eventBus, view, proxy);
        
        this.placeManager = placeManager;
    }

    @Override
    protected void onBind() {
      super.onBind();
      registerHandler(getView().getGotoEventsPageButton().addClickHandler(
          new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	PlaceRequest nextPlace = new PlaceRequest.Builder().nameToken(PageNameTokens.eventsPage).build();
            	placeManager.revealPlace(nextPlace, false);
            }
          }));
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RootPagePresenter.TYPE_SetMainContent, this);
    }
}
