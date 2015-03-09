package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanelPlaceSelectionEvent;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.partials.header.EventHeader;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;

public class TabletAndDesktopEventView extends Composite implements EventRegattaView {
    private static final ApplicationHistoryMapper historyMapper = GWT
            .<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopEventView> {
    }

    @UiField
    TextMessages i18n;

    @UiField(provided = true)
    TabPanel<EventRegattaView.Presenter> tabPanelUi;

    @UiField(provided = true)
    EventHeader eventHeader;

    public TabletAndDesktopEventView() {
    }

    @Override
    public void registerPresenter(final Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<>(currentPresenter, historyMapper);

        eventHeader = new EventHeader(currentPresenter);

        initWidget(uiBinder.createAndBindUi(this));

        initBreadCrumbs();
    }

    @Override
    public void navigateTabsTo(AbstractEventRegattaPlace place) {
        tabPanelUi.activatePlace(place);
    }

    @SuppressWarnings("unchecked")
    @UiHandler("tabPanelUi")
    public void onTabSelection(TabPanelPlaceSelectionEvent e) {
        currentPresenter.handleTabPlaceSelection((TabView<?, EventRegattaView.Presenter>) e.getSelectedActivity());
    }

    private void initBreadCrumbs() {
        addBreadCrumbItem(i18n.home(), currentPresenter.getHomeNavigation());
        addBreadCrumbItem(i18n.events(), currentPresenter.getEventsNavigation());
        if(currentPresenter.getCtx().getEventDTO().getType() == EventType.SERIES_EVENT) {
            addBreadCrumbItem(currentPresenter.getCtx().getEventDTO().getSeriesName(),  currentPresenter.getCurrentEventSeriesNavigation());
        }
        addBreadCrumbItem(currentPresenter.getCtx().getEventDTO().getName(), currentPresenter.getCurrentEventNavigation());
        
        if(currentPresenter.showRegattaMetadata()) {
            addBreadCrumbItem(currentPresenter.getRegattaMetadata().getDisplayName(), currentPresenter.getCurrentRegattaOverviewNavigation());
        }
    }

    private void addBreadCrumbItem(String label, final PlaceNavigation<?> placeNavigation) {
        tabPanelUi.addBreadcrumbItem(label, placeNavigation.getTargetUrl(), new Runnable() {
            @Override
            public void run() {
                placeNavigation.goToPlace();
            }
        });
    }

}
