package com.sap.sailing.gwt.home.client.place.fakeseries;

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
import com.sap.sailing.gwt.home.client.place.fakeseries.partials.header.SeriesHeader;

public class TabletAndDesktopSeriesView extends Composite implements SeriesTabsView {
    private static final ApplicationHistoryMapper historyMapper = GWT.<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private SeriesTabsView.Presenter currentPresenter;

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopSeriesView> {
    }

    @UiField TextMessages i18n;
    
    @UiField(provided = true)
    TabPanel<SeriesTabsView.Presenter> tabPanelUi;
    
    @UiField(provided = true)
    SeriesHeader seriesHeader;

    public TabletAndDesktopSeriesView() {
    }

    @Override
    public void registerPresenter(final SeriesTabsView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<>(currentPresenter, historyMapper);
        
        seriesHeader = new SeriesHeader(currentPresenter);
        
        initWidget(uiBinder.createAndBindUi(this));

        initBreadCrumbs();
    }

    @Override
    public void navigateTabsTo(AbstractSeriesTabPlace place) {
        tabPanelUi.activatePlace(place);
    }

    @SuppressWarnings("unchecked")
    @UiHandler("tabPanelUi")
    public void onTabSelection(TabPanelPlaceSelectionEvent e) {
        currentPresenter.handleTabPlaceSelection((TabView<?, SeriesTabView.Presenter>) e.getSelectedActivity());
    }
    
    private void initBreadCrumbs() {
        addBreadCrumbItem(i18n.home(), currentPresenter.getHomeNavigation());
        addBreadCrumbItem(i18n.events(), currentPresenter.getEventsNavigation());
        addBreadCrumbItem(currentPresenter.getCtx().getSeriesDTO().getDisplayName(),  currentPresenter.getCurrentEventSeriesNavigation());
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
