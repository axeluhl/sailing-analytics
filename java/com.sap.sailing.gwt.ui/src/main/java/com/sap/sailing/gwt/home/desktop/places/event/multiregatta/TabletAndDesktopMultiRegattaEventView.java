package com.sap.sailing.gwt.home.desktop.places.event.multiregatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanelPlaceSelectionEvent;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.desktop.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.desktop.partials.sailorinfo.SailorInfo;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TabletAndDesktopMultiRegattaEventView extends Composite implements EventMultiregattaView {
    
    private static final ApplicationHistoryMapper historyMapper = GWT.<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopMultiRegattaEventView> {
    }
    
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField StringMessages i18n;
    
    @UiField(provided = true) TabPanel<EventMultiregattaView.Presenter> tabPanelUi;
    @UiField(provided = true) EventHeader eventHeader;
    
    private Presenter currentPresenter;

    public TabletAndDesktopMultiRegattaEventView() {
    }

    @Override
    public void registerPresenter(final Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<>(currentPresenter, historyMapper);
        eventHeader = new EventHeader(currentPresenter);
        initWidget(uiBinder.createAndBindUi(this));
        initBreadCrumbs();
        
        String sailorsInfoURL = currentPresenter.getEventDTO().getSailorsInfoWebsiteURL();
        if(sailorsInfoURL != null && ! sailorsInfoURL.isEmpty()) {
            tabPanelUi.addTabExtension(new SailorInfo(sailorsInfoURL));
        }
    }

    @Override
    public void navigateTabsTo(AbstractMultiregattaEventPlace place) {
        tabPanelUi.activatePlace(place);
        StringBuilder titleBuilder = new StringBuilder(TextMessages.INSTANCE.sapSailing()).append(" - ");

        titleBuilder.append(currentPresenter.showRegattaMetadata() ? currentPresenter.getRegattaMetadata()
                .getDisplayName() : currentPresenter.getEventDTO().getDisplayName());
        String currentTabTitle = tabPanelUi.getCurrentTabTitle();
        if (currentTabTitle != null && !currentTabTitle.isEmpty()) {
            titleBuilder.append(" - ").append(currentTabTitle);
        }
        Window.setTitle(titleBuilder.toString());
    }

    @SuppressWarnings("unchecked")
    @UiHandler("tabPanelUi")
    public void onTabSelection(TabPanelPlaceSelectionEvent e) {
        currentPresenter.handleTabPlaceSelection((TabView<?, EventMultiregattaView.Presenter>) e.getSelectedActivity());
    }
    
    private void initBreadCrumbs() {
        addBreadCrumbItem(i18n.home(), currentPresenter.getHomeNavigation());
        addBreadCrumbItem(i18n.events(), currentPresenter.getEventsNavigation());
        addBreadCrumbItem(currentPresenter.getEventDTO().getDisplayName(), currentPresenter.getCurrentEventNavigation());
    }
    
    private void addBreadCrumbItem(String label, final PlaceNavigation<?> placeNavigation) {
        tabPanelUi.addBreadcrumbItem(label, placeNavigation.getTargetUrl(), new Runnable() {
            @Override
            public void run() {
                placeNavigation.goToPlace();
            }
        });
    }

    @Override
    public void showErrorInCurrentTab(IsWidget errorView) {
        tabPanelUi.overrideCurrentContentInTab(errorView);
    }
}
