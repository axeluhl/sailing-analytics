package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.PlaceContextProvider;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.home.client.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class TabletAndDesktopEventView extends Composite implements EventView {
    private static final ApplicationHistoryMapper historyMapper = GWT
            .<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private Presenter currentPresenter;

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopEventView> {
    }

    @UiField(provided = true)
    TabPanel<EventContext> tabPanelUi;

    public TabletAndDesktopEventView() {
        tabPanelUi = new TabPanel<EventContext>(new PlaceContextProvider<EventContext>() {

            @Override
            public EventContext getContext() {

                return currentPresenter.getCtx();
            }
        }, historyMapper);

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void registerPresenter(Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

    @Override
    public void navigateTabsTo(EventPlace place) {
        tabPanelUi.gotoPlace(place);
    }

}
