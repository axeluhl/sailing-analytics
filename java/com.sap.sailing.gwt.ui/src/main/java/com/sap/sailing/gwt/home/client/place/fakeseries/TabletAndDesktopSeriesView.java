package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.PlaceContextProvider;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanelPlaceSelectionEvent;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.fakeseries.partials.header.SeriesHeader;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TabletAndDesktopSeriesView extends Composite implements SeriesTabsView {
    private static final ApplicationHistoryMapper historyMapper = GWT.<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private SeriesTabsView.Presenter currentPresenter;

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopSeriesView> {
    }

    @UiField StringMessages i18n;
    
    @UiField(provided = true)
    TabPanel<SeriesContext, SeriesTabsView.Presenter> tabPanelUi;
    
    @UiField(provided = true)
    SeriesHeader seriesHeader;

    public TabletAndDesktopSeriesView() {
    }

    @Override
    public void registerPresenter(final SeriesTabsView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<>(new PlaceContextProvider<SeriesContext>() {
            
            @Override
            public SeriesContext getContext() {
                
                return currentPresenter.getCtx();
            }
        }, currentPresenter, historyMapper);
        
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
    public void onTabSelection(TabPanelPlaceSelectionEvent<?> e) {
        currentPresenter.handleTabPlaceSelection((TabView<?, SeriesTabView.Presenter>) e.getSelectedActivity());
    }
    
    private void initBreadCrumbs() {
        addBreadCrumbItem(i18n.home(), new StartPlace());
        addBreadCrumbItem(i18n.events(), new EventsPlace());
        addBreadCrumbItem(currentPresenter.getCtx().getEventDTO().getName(), new SeriesDefaultPlace(currentPresenter.getCtx()));
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
