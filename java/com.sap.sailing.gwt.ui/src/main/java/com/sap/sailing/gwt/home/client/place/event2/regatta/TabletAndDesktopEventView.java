package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.PlaceContextProvider;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanelPlaceSelectionEvent;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.home.client.place.event2.header.EventHeader;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TabletAndDesktopEventView extends Composite implements EventRegattaView {
    private static final ApplicationHistoryMapper historyMapper = GWT
            .<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopEventView> {
    }

    @UiField StringMessages i18n;
    
    @UiField(provided = true)
    TabPanel<EventContext, EventRegattaView.Presenter> tabPanelUi;
    
    @UiField(provided = true)
    EventHeader eventHeader;

    public TabletAndDesktopEventView() {
    }

    @Override
    public void registerPresenter(final Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<>(new PlaceContextProvider<EventContext>() {
            
            @Override
            public EventContext getContext() {
                return currentPresenter.getCtx();
            }
        }, currentPresenter, historyMapper);
        
        eventHeader = new EventHeader(currentPresenter);
        
        initWidget(uiBinder.createAndBindUi(this));

        initBreadCrumbs();
    }

    @Override
    public void navigateTabsTo(AbstractEventRegattaPlace place) {
        tabPanelUi.activatePlace(place);
    }

    @UiHandler("tabPanelUi")
    public void onTabSelection(TabPanelPlaceSelectionEvent<?> e) {
        currentPresenter.handleTabPlaceSelection((TabView<?, EventContext, EventRegattaView.Presenter>) e.getSelectedActivity());
    }
    
    private void initBreadCrumbs() {
      addBreadCrumbItem(i18n.home(), new StartPlace());
      addBreadCrumbItem(i18n.events(), new EventsPlace());
      addBreadCrumbItem(currentPresenter.getCtx().getEventDTO().getName(), new EventDefaultPlace(currentPresenter.getCtx()));
      // TODO additional item for multi Regatta
  }
  
  private void addBreadCrumbItem(String label, final Place place) {
      tabPanelUi.addBreadcrumbItem(label, currentPresenter.getUrl(place), new Runnable() {
          @Override
          public void run() {
              currentPresenter.navigateTo(place);
          }
      });
  }

}
