package com.sap.sailing.gwt.home.client.app.event;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import com.sap.sailing.gwt.home.client.SailingEventsServiceAsync;
import com.sap.sailing.gwt.home.client.app.AbstractRootPagePresenter;
import com.sap.sailing.gwt.home.client.shared.PageNameConstants;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

public class EventPagePresenter extends Presenter<EventPagePresenter.MyView, EventPagePresenter.MyProxy> {
    private final PlaceManager placeManager;
    private final SailingEventsServiceAsync sailingEventsService;

    private EventDTO event;
    private String eventIdParam;

    public interface MyView extends View {
        public void setEvent(EventDTO event);
    }

    /**
     * Child presenters can fire a RevealContentEvent with TYPE_SetMainContent to set themselves as children of this
     * presenter.
     */
    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    @ProxyCodeSplit
    @NameToken(PageNameConstants.eventPage)
    public interface MyProxy extends ProxyPlace<EventPagePresenter> {
    }

    @Inject
    public EventPagePresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
            SailingEventsServiceAsync sailingEventsService) {
        super(eventBus, view, proxy);

        this.placeManager = placeManager;
        this.sailingEventsService = sailingEventsService;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, AbstractRootPagePresenter.TYPE_SetMainContent, this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        eventIdParam = request.getParameter("id", "");
    }

    @Override
    protected void onReset() {
        super.onReset();

        sailingEventsService.getEventById(eventIdParam, new AsyncCallback<EventDTO>() {
            @Override
            public void onSuccess(EventDTO result) {
                event = result;
                getView().setEvent(event);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Shit happens");
            }
        });
    }
}
