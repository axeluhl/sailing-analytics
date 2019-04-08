package com.sap.sailing.gwt.home.desktop.places.event.multiregatta;

import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.desktop.places.event.EventClientFactory;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;

/**
 * Base Activity for all desktop multi-regatta-event pages.
 *
 * @param <PLACE> The concrete {@link AbstractMultiregattaEventPlace} subclass, this instance is bound to.
 */
public class EventMultiregattaActivity extends AbstractEventActivity<AbstractMultiregattaEventPlace> implements EventMultiregattaView.Presenter {

    private EventMultiregattaView currentView = new TabletAndDesktopMultiRegattaEventView();

    public EventMultiregattaActivity(AbstractMultiregattaEventPlace place, EventViewDTO eventDTO, EventClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator, NavigationPathDisplay navigationPathDisplay) {
        super(place, eventDTO, clientFactory, homePlacesNavigator);
        
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        final List<NavigationItem> navigationItems = getNavigationPathToEventLevel();
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
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

    @Override
    public PlaceNavigation<SeriesDefaultPlace> getCurrentEventSeriesNavigation() {
        return getEventSeriesNavigation(getEventDTO().getSeriesData());
    }
}
