package com.sap.sailing.gwt.home.client.place.event.partials.countdown;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MainCss;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.EventView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.partials.countdown.CountdownResources.LocalCss;
import com.sap.sailing.gwt.home.client.shared.stage.StageResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewRaceTickerStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewRegattaTickerStageDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventOverviewTickerStageDTO;

public class Countdown extends Composite {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static final TextMessages I18N_UBI = TextMessages.INSTANCE;
    private static final LocalCss CSS = CountdownResources.INSTANCE.css();
    private static final MainCss MAIN_CSS = SharedResources.INSTANCE.mainCss();

    private static CountdownUiBinder uiBinder = GWT.create(CountdownUiBinder.class);

    interface CountdownUiBinder extends UiBinder<Widget, Countdown> {
    }

    @UiField SimplePanel tickerContainer;
    @UiField HeadingElement countdownTitle;
    @UiField DivElement countdownInfo;
    @UiField HeadingElement infoTitle;
    @UiField(provided = true) NavigationAnchor navigationButton;
    @UiField DivElement image;

    public Countdown(EventView.Presenter presenter) {
        CSS.ensureInjected();
        this.navigationButton = new NavigationAnchor(presenter);
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(EventOverviewTickerStageDTO data) {
        String stageImageUrl = StageResources.INSTANCE.defaultStageEventTeaserImage().getSafeUri().asString();
        if(data.getStageImageUrl() != null) {
            stageImageUrl = data.getStageImageUrl();
        }
        image.getStyle().setBackgroundImage("url(\"" + stageImageUrl + "\")");
        
        navigationButton.removeStyleName(MAIN_CSS.buttonred());
        navigationButton.removeStyleName(MAIN_CSS.buttonprimary());
        if (data instanceof EventOverviewRaceTickerStageDTO) {
            this.updateUi(I18N.nextRaceStartingIn(), data.getTickerInfo());
            this.navigationButton.linkToRaceViewer((EventOverviewRaceTickerStageDTO) data);
        } else if (data instanceof EventOverviewRegattaTickerStageDTO) {
            this.updateUi(I18N.startingIn(data.getTickerInfo()), data.getTickerInfo());
            this.navigationButton.linkToRegatta((EventOverviewRegattaTickerStageDTO) data);
        } else {
            this.updateUi(data.getTickerInfo() != null ? I18N.startingIn(data.getTickerInfo()) : null, null);
        }
        
        if(data.getStartTime() != null) {
            this.tickerContainer.setWidget(new CountdownTicker(data.getStartTime()));
        } else {
            this.tickerContainer.setWidget(null);
        }
    }

    private void updateUi(String title, String info) {
        this.countdownTitle.setInnerText(title);
        this.infoTitle.setInnerText(info);
        this.countdownInfo.getStyle().setDisplay(info != null ? Display.BLOCK : Display.NONE);
    }

    @UiHandler("navigationButton")
    void handleNavigationButtonClick(ClickEvent event) {
        this.navigationButton.onClick(event);
    }

    private class NavigationAnchor extends Anchor {

        private final EventView.Presenter presenter;
        private PlaceNavigation<?> currentPlaceNavigation;

        private NavigationAnchor(Presenter presenter) {
            this.presenter = presenter;
        }

        private void linkToRaceViewer(EventOverviewRaceTickerStageDTO data) {
            // TODO implement correctly
//            String url = presenter.getRaceViewerURL(data.getRegattaAndRaceIdentifier());
            String url = "";
            this.update(MAIN_CSS.buttonprimary(), MAIN_CSS.buttonred(), I18N_UBI.watchNow(), null, url);
        }

        private void linkToRegatta(EventOverviewRegattaTickerStageDTO data) {
            PlaceNavigation<?> nav = presenter.getRegattaNavigation(data.getRegattaIdentifier().getRegattaName());
            this.update(MAIN_CSS.buttonred(), MAIN_CSS.buttonprimary(), I18N_UBI.regattaDetails(), nav,
                    nav.getTargetUrl());
        }
        
        private void update(String removeStyle, String addStyle, String text, PlaceNavigation<?> navigation, String url) {
            this.removeStyleName(removeStyle);
            this.addStyleName(addStyle);
            this.setText(text);
            this.currentPlaceNavigation = navigation;
            this.setHref(url);
        }

        private void onClick(ClickEvent event) {
            if (LinkUtil.handleLinkClick(event.getNativeEvent().<Event> cast())) {
                if (currentPlaceNavigation != null) {
                    event.preventDefault();
                    currentPlaceNavigation.goToPlace();
                }
            }
        }
    }
}
