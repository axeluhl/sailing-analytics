package com.sap.sailing.gwt.home.desktop.places.event.multiregatta;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.desktop.places.event.EventClientFactory;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.EventView.PlaceCallback;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

public class EventMultiregattaActivity extends AbstractEventActivity<AbstractMultiregattaEventPlace> implements EventMultiregattaView.Presenter {

    private EventMultiregattaView currentView = new TabletAndDesktopMultiRegattaEventView();

    public EventMultiregattaActivity(AbstractMultiregattaEventPlace place, EventViewDTO eventDTO, EventClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
        super(place, eventDTO, clientFactory, homePlacesNavigator);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }
    
    @Override
    public boolean needsSelectionInHeader() {
        return false;
    }
    
    @Override
    public void forPlaceSelection(PlaceCallback callback) {
    }
    
    @Override
    public boolean showRegattaMetadata() {
        return false;
    }

    @Override
    protected EventView<AbstractMultiregattaEventPlace, ?> getView() {
        return currentView;
    }

}
