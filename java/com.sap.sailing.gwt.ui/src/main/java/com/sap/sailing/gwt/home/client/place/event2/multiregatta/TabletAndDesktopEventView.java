package com.sap.sailing.gwt.home.client.place.event2.multiregatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.PlaceContextProvider;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanelPlaceSelectionEvent;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.header.EventHeader;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TabletAndDesktopEventView extends Composite implements EventMultiregattaView {
    private static final ApplicationHistoryMapper historyMapper = GWT
            .<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopEventView> {
    }

    @UiField StringMessages i18n;
    
    @UiField(provided = true)
    TabPanel<EventContext> tabPanelUi;
    
    @UiField(provided = true)
    EventHeader eventHeader;

    public TabletAndDesktopEventView() {
    }

    @Override
    public void registerPresenter(final Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<EventContext>(new PlaceContextProvider<EventContext>() {
            
            @Override
            public EventContext getContext() {
                
                return currentPresenter.getCtx();
            }
        }, historyMapper);
        
        eventHeader = new EventHeader(currentPresenter);
        
        initWidget(uiBinder.createAndBindUi(this));

        initBreadCrumbs();
    }

    @Override
    public void navigateTabsTo(AbstractMultiregattaEventPlace place) {
        tabPanelUi.activatePlace(place);
    }

    @UiHandler("tabPanelUi")
    public void onTabSelection(TabPanelPlaceSelectionEvent<?> e) {
        currentPresenter.handleTabPlaceSelection((TabActivity<?, EventContext>) e.getSelectedActivity());
    }
    
    private void initBreadCrumbs() {
//      addBreadCrumbItem(i18n.home(), placeNavigator.getHomeNavigation());
//      addBreadCrumbItem(i18n.events(), placeNavigator.getEventsNavigation());
      // TODO series, event ...
      // TODO dummy implementation
        tabPanelUi.addBreadcrumbItem(i18n.home(), "TODO" /* placeNavigator.getHomeNavigation().getTargetUrl() */, new Runnable() {
          @Override
          public void run() {
              // TODO
//              presenter.
//              placeNavigator.getHomeNavigation().getPlace()
          }
      });
        tabPanelUi.addBreadcrumbItem(i18n.events(), "TODO" /* placeNavigator.getEventsNavigation().getTargetUrl() */, new Runnable() {
          @Override
          public void run() {
              // TODO
//              presenter.
//              placeNavigator.getEventsNavigation().getPlace()
          }
      });
        tabPanelUi.addBreadcrumbItem(currentPresenter.getCtx().getEventDTO().getName(), "TODO", new Runnable() {
          @Override
          public void run() {
              // TODO
          }
      });
  }
  
  private void addBreadCrumbItem(String label, final PlaceNavigation<?> placeNavigation) {
      tabPanelUi.addBreadcrumbItem(label, placeNavigation.getTargetUrl(), new Runnable() {
          @Override
          public void run() {
              currentPresenter.navigateTo(placeNavigation.getPlace());
          }
      });
  }

}
