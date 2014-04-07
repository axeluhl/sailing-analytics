package com.sap.sailing.gwt.home.client.eventspage;

import java.util.List;

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
import com.sap.sailing.gwt.home.client.RootPagePresenter;
import com.sap.sailing.gwt.home.client.SailingEventsServiceAsync;
import com.sap.sailing.gwt.home.client.shared.PageNameTokens;
import com.sap.sailing.gwt.home.shared.dto.EventDTO;

public class EventsPagePresenter extends Presenter<EventsPagePresenter.MyView, EventsPagePresenter.MyProxy> {
    private final PlaceManager placeManager;
    private final SailingEventsServiceAsync sailingEventsService; 

    private List<EventDTO> events; 

    public interface MyView extends View {
    	public void setEvents(List<EventDTO> events);
    }

    /**
     * Child presenters can fire a RevealContentEvent with TYPE_SetMainContent to set themselves
     * as children of this presenter.
     */
    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<RevealContentHandler<?>>();

    @ProxyCodeSplit
    @NameToken(PageNameTokens.eventsPage)
    public interface MyProxy extends ProxyPlace<EventsPagePresenter> {
    }

    @Inject
    public EventsPagePresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager,
    		SailingEventsServiceAsync sailingEventsService) {
        super(eventBus, view, proxy);
        
        this.placeManager = placeManager;
        this.sailingEventsService = sailingEventsService;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RootPagePresenter.TYPE_SetMainContent, this);
    }

	@Override
	protected void onReset() {
		super.onReset();
		
		sailingEventsService.getEvents(new AsyncCallback<List<EventDTO>>() {
			@Override
			public void onSuccess(List<EventDTO> result) {
				events = result;
				getView().setEvents(events);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Shit happens");
			}
		}); 
	}
}
