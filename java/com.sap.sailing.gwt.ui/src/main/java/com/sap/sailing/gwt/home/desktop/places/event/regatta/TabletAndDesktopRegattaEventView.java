package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.EventType;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanel;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabPanelPlaceSelectionEvent;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.partials.eventheader.EventHeader;
import com.sap.sailing.gwt.home.desktop.partials.sailorinfo.SailorInfo;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesDefaultPlace;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.LinkUtil;

public class TabletAndDesktopRegattaEventView extends Composite implements EventRegattaView {
    
    private static final ApplicationHistoryMapper historyMapper = GWT.<ApplicationHistoryMapper> create(ApplicationHistoryMapper.class);

    interface MyBinder extends UiBinder<Widget, TabletAndDesktopRegattaEventView> {
    }
    
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField StringMessages i18n;

    @UiField(provided = true) TabPanel<AbstractEventRegattaPlace, EventRegattaView.Presenter, RegattaTabView<AbstractEventRegattaPlace>> tabPanelUi;
    @UiField(provided = true) EventHeader eventHeader;
    
    private Presenter currentPresenter;

    public TabletAndDesktopRegattaEventView() {
    }

    @Override
    public void registerPresenter(final Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
        tabPanelUi = new TabPanel<>(currentPresenter, historyMapper);
        eventHeader = new EventHeader(currentPresenter);
        initWidget(uiBinder.createAndBindUi(this));
        
        if(currentPresenter.getEventDTO().getType() == EventType.SERIES) {
            final PlaceNavigation<SeriesDefaultPlace> currentEventSeriesNavigation = currentPresenter.getCurrentEventSeriesNavigation();
            Anchor seriesAnchor = new Anchor(i18n.overallLeaderboardSelection());
            seriesAnchor.setHref(currentEventSeriesNavigation.getTargetUrl());
            seriesAnchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (LinkUtil.handleLinkClick(event.getNativeEvent().<Event>cast())) {
                        event.preventDefault();
                        currentEventSeriesNavigation.goToPlace();
                    }
                }
            });
            seriesAnchor.setStyleName(SharedResources.INSTANCE.mainCss().button());
            seriesAnchor.addStyleName(SharedResources.INSTANCE.mainCss().buttonprimary());
            Style style = seriesAnchor.getElement().getStyle();
            style.setFontSize(16, Unit.PX);
            style.setPadding(0.75, Unit.EM);
            tabPanelUi.addTabExtension(seriesAnchor);
        } else {
            String sailorsInfoURL = currentPresenter.getEventDTO().getSailorsInfoWebsiteURL();
            if(sailorsInfoURL != null && ! sailorsInfoURL.isEmpty()) {
                tabPanelUi.addTabExtension(new SailorInfo(sailorsInfoURL));
            }
        }
    }

    @Override
    public void navigateTabsTo(AbstractEventRegattaPlace place) {
        tabPanelUi.activatePlace(place);
        StringBuilder titleBuilder = new StringBuilder(StringMessages.INSTANCE.sapSailing()).append(" - ");

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
        currentPresenter.handleTabPlaceSelection((TabView<?, EventRegattaView.Presenter>) e.getSelectedActivity());
    }

    @Override
    public void showErrorInCurrentTab(IsWidget errorView) {
        tabPanelUi.overrideCurrentContentInTab(errorView);
    }

}
