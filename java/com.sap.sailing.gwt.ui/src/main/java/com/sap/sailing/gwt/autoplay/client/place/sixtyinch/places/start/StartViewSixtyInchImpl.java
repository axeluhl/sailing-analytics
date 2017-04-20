package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInchImpl;
import com.sap.sailing.gwt.autoplay.client.place.start.StartView;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.GWTLocaleUtil;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;

public class StartViewSixtyInchImpl extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, StartViewSixtyInchImpl> {
    }

    @UiField(provided=true) ListBox localeSelectionBox;
    @UiField(provided=true) ListBox eventSelectionBox;
    @UiField(provided=true) ListBox leaderboardSelectionBox;
    @UiField Button startAutoPlayButton;
    @UiField DivElement leaderboardSelectionUi;
    @UiField DivElement screenConfigurationUi;
    
    private final List<EventDTO> events;
    private AutoPlayClientFactorySixtyInchImpl clientFactory;
    
    public StartViewSixtyInchImpl(AutoPlayClientFactorySixtyInchImpl clientFactory) {
        this.events = new ArrayList<EventDTO>();
        this.clientFactory = clientFactory;
        
        eventSelectionBox = new ListBox();
        eventSelectionBox.setMultipleSelect(false);
        leaderboardSelectionBox = new ListBox();
        leaderboardSelectionBox.setMultipleSelect(false);
        localeSelectionBox = new ListBox();
        localeSelectionBox.setMultipleSelect(false);
        
        LocaleInfo currentLocale = LocaleInfo.getCurrentLocale();
        int i = 0;
        for (String localeName : GWTLocaleUtil.getAvailableLocales()) {
            String displayName = GWTLocaleUtil.getDecoratedLanguageDisplayNameWithDefaultLocaleSupport(localeName);
            localeSelectionBox.addItem(displayName, localeName);
            if (currentLocale.getLocaleName().equals(localeName)) {
                localeSelectionBox.setSelectedIndex(i);
            }
            i++;
        }

        initWidget(uiBinder.createAndBindUi(this));
        this.ensureDebugId("AutoPlayStartView");

        leaderboardSelectionUi.getStyle().setVisibility(Visibility.HIDDEN);
        screenConfigurationUi.getStyle().setVisibility(Visibility.HIDDEN);
        
        startAutoPlayButton.setEnabled(false);
        startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
    }

    @Override
    public void setEvents(List<EventDTO> events) {
        this.events.clear();
        this.events.addAll(events);
        eventSelectionBox.addItem(StringMessages.INSTANCE.pleaseSelectAnEvent());
        for (EventDTO event : Util.sortNamedCollection(events)) {
            eventSelectionBox.addItem(event.getName());
        }
    }

    @UiHandler("eventSelectionBox")
    void onEventSelectionChange(ChangeEvent event) {
        EventDTO selectedEvent = getSelectedEvent();
        if(selectedEvent != null) {
            leaderboardSelectionBox.clear();
            leaderboardSelectionBox.addItem(StringMessages.INSTANCE.selectALeaderboard());
            for(LeaderboardGroupDTO leaderboardGroup: selectedEvent.getLeaderboardGroups()) {
                for(StrippedLeaderboardDTO leaderboard: leaderboardGroup.getLeaderboards()) {
                    leaderboardSelectionBox.addItem(leaderboard.name);
                }
            }
        }
        leaderboardSelectionUi.getStyle().setVisibility(selectedEvent != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    @UiHandler("leaderboardSelectionBox")
    void onLeaderboardSelectionChange(ChangeEvent event) {
        String selectedLeaderboardName = getSelectedLeaderboardName();
        if(selectedLeaderboardName != null) {
            startAutoPlayButton.setEnabled(true);
            startAutoPlayButton.removeStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        } else {
            startAutoPlayButton.setEnabled(false);
            startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        }
        screenConfigurationUi.getStyle().setVisibility(selectedLeaderboardName != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    @UiHandler("localeSelectionBox") 
    void onLocaleSelectionChange(ChangeEvent event) {
        String selectedLocale = getSelectedLocale();
        LocaleChangeEvent localeChangeEvent = new LocaleChangeEvent(selectedLocale);
        clientFactory.getEventBus().fireEvent(localeChangeEvent);
    }
    
    @UiHandler("startAutoPlayButton")
    void startAutoPlayClicked(ClickEvent event) {
        EventDTO selectedEvent = getSelectedEvent();
        String selectedLeaderboardName = getSelectedLeaderboardName();
        
        if(selectedEvent != null && selectedLeaderboardName != null) {
            clientFactory.getPlaceNavigator().goToPlayerSixtyInch(
                    new SixtyInchSetting(selectedEvent.id, selectedLeaderboardName),
                    clientFactory);
        }
    }

    private String getSelectedLocale() {
        String result = null;
        int selectedIndex = localeSelectionBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedLocale = localeSelectionBox.getValue(selectedIndex);
            for (String localeName : GWTLocaleUtil.getAvailableLocales()) {
                if (selectedLocale.equals(localeName)) {
                    result = localeName;
                    break;
                }
            }
        }
        return result;
    }

    private String getSelectedLeaderboardName() {
        String result = null;
        int selectedIndex = leaderboardSelectionBox.getSelectedIndex();
        if (selectedIndex > 0) {
            result = leaderboardSelectionBox.getItemText(selectedIndex);
        }
        return result;
    }

    private EventDTO getSelectedEvent() {
        EventDTO result = null;
        int selectedIndex = eventSelectionBox.getSelectedIndex();
        if(events != null && selectedIndex > 0) {
            String selectedItemText = eventSelectionBox.getItemText(selectedIndex);
            for(EventDTO event: events) {
                if(event.getName().equals(selectedItemText)) {
                    result = event;
                    break;
                }
            }
        }
        return result;
    }
}
