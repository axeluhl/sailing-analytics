package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.place.player.PlayerPlace;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeader;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.perspective.Perspective;
import com.sap.sailing.gwt.ui.client.shared.perspective.PerspectiveConfigurationComposite;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspective;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceViewerPerspective;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;

public class DesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, DesktopStartView> {
    }

    @UiField(provided=true) SAPHeader sapHeader;
    @UiField(provided=true) ListBox localeSelectionBox;
    @UiField(provided=true) ListBox eventSelectionBox;
    @UiField(provided=true) ListBox leaderboardSelectionBox;
    @UiField CheckBox leaderboardAutoZoomBox;
    @UiField CheckBox startInFullscreenModeBox;
    @UiField CheckBox autoSelectMediaBox;
    @UiField TextBox leaderboardZoomBox;
    @UiField Button startAutoPlayButton;
    @UiField DivElement leaderboardSelectionUi;
    @UiField DivElement leaderboardZoomDiv;
    @UiField DivElement leaderboardAutoZoomDiv;
    @UiField DivElement screenConfiguraionUi;
    
    @UiField CheckBox autoSwitchToRaceboard;
    @UiField TextBox timeToRaceStartInSeconds;
    @UiField TabPanel componentConfigurationTabPanel;
    
    private final PlaceNavigator navigator;
    private final EventBus eventBus;
    private final List<EventDTO> events;
    
    private Map<String, String> raceboardParameters;
    private Map<String, String> leaderboardParameters;
    
    private List<Perspective> supportedPerspectives;

    private LeaderboardPerspective leaderboardPerspective;
    private RaceViewerPerspective raceViewerPerspective;
    
    private final int defaultTimeToStartTimeInSeconds = 180;

    public DesktopStartView(PlaceNavigator navigator, EventBus eventBus) {
        super();
        this.navigator = navigator;
        this.eventBus = eventBus;
        this.events = new ArrayList<EventDTO>();
        this.raceboardParameters = new HashMap<String, String>();
        this.leaderboardParameters = new HashMap<String, String>();
        this.supportedPerspectives = new ArrayList<Perspective>();
        
        sapHeader = new SAPHeader("Auto player configuration", false);
        eventSelectionBox = new ListBox();
        eventSelectionBox.setMultipleSelect(false);
        leaderboardSelectionBox = new ListBox();
        leaderboardSelectionBox.setMultipleSelect(false);
        localeSelectionBox = new ListBox();
        localeSelectionBox.setMultipleSelect(false);

        leaderboardPerspective = new LeaderboardPerspective();
        raceViewerPerspective = new RaceViewerPerspective(); 
        
        supportedPerspectives.add(leaderboardPerspective);
        supportedPerspectives.add(raceViewerPerspective);
        
        LocaleInfo currentLocale = LocaleInfo.getCurrentLocale();
        int i = 0;
        for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
            if(!localeName.equals("default")) {
                String displayName = LocaleInfo.getLocaleNativeDisplayName(localeName);
                localeSelectionBox.addItem(displayName);
                if(currentLocale.getLocaleName().equals(localeName)) {
                    localeSelectionBox.setSelectedIndex(i);
                }
                i++;
            }
         }
        
        initWidget(uiBinder.createAndBindUi(this));
        this.ensureDebugId("AutoPlayStartView");

        leaderboardAutoZoomBox.setValue(true);
        leaderboardZoomBox.setEnabled(false);
        startInFullscreenModeBox.setValue(true);
        autoSwitchToRaceboard.setValue(true);
        timeToRaceStartInSeconds.setValue(String.valueOf(defaultTimeToStartTimeInSeconds));

        leaderboardSelectionUi.getStyle().setVisibility(Visibility.HIDDEN);
        screenConfiguraionUi.getStyle().setVisibility(Visibility.HIDDEN);
        
        componentConfigurationTabPanel.setVisible(false);
        
        startAutoPlayButton.setEnabled(false);
        startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
    }

    @Override
    public void setEvents(List<EventDTO> events) {
        this.events.clear();
        this.events.addAll(events);
        
        eventSelectionBox.addItem("Please select an event");
        for(EventDTO event: events) {
            eventSelectionBox.addItem(event.getName());
        }
    }
    
    @UiHandler("eventSelectionBox")
    void onEventSelectionChange(ChangeEvent event) {
        EventDTO selectedEvent = getSelectedEvent();
        if(selectedEvent != null) {
            leaderboardSelectionBox.clear();
            leaderboardSelectionBox.addItem("Please select a leaderboard");
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

            StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard();
            
            leaderboardPerspective.setLeaderboard(selectedLeaderboard);
            raceViewerPerspective.setLeaderboard(selectedLeaderboard);
            
            componentConfigurationTabPanel.clear();
            for(Perspective perspective: supportedPerspectives) {
                componentConfigurationTabPanel.add(new PerspectiveConfigurationComposite(perspective), perspective.getPerspectiveName());
            }
            componentConfigurationTabPanel.getTabBar().selectTab(0);
        } else {
            startAutoPlayButton.setEnabled(false);
            startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        }
        componentConfigurationTabPanel.setVisible(selectedLeaderboardName != null);
        screenConfiguraionUi.getStyle().setVisibility(selectedLeaderboardName != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }
    
    @UiHandler("localeSelectionBox") 
    void onLocaleSelectionChange(ChangeEvent event) {
        String selectedLocale = getSelectedLocale();
        LocaleChangeEvent localeChangeEvent = new LocaleChangeEvent(selectedLocale);
        eventBus.fireEvent(localeChangeEvent);
    }
    
    @UiHandler("startAutoPlayButton")
    void startAutoPlayClicked(ClickEvent event) {
        EventDTO selectedEvent = getSelectedEvent();
        String selectedLeaderboardName = getSelectedLeaderboardName();
        String leaderboardZoom = getLeaderboardZoom();
        Boolean isAutoSelectMedia = autoSelectMediaBox.getValue();
        
        if(selectedEvent != null && selectedLeaderboardName != null) {
            leaderboardParameters.put(PlayerPlace.PARAM_LEADEROARD_ZOOM, leaderboardZoom);
            leaderboardParameters.put(PlayerPlace.PARAM_LEADEROARD_NAME, selectedLeaderboardName);
            raceboardParameters.put(PlayerPlace.PARAM_RACEBOARD_AUTOSELECT_MEDIA, String.valueOf(isAutoSelectMedia));

            navigator.goToPlayer(selectedEvent.id.toString(), startInFullscreenModeBox.getValue(), 
                    leaderboardParameters, raceboardParameters);
        }
    }

    private void handleLeaderboardSettingsClick(ClickEvent e) {
        LeaderboardSettingsDialog dialog = new LeaderboardSettingsDialog(StringMessages.INSTANCE, getSelectedLeaderboard(), 
                new DialogCallback<LeaderboardSettings>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(LeaderboardSettings newSettings) {
            }
        });
        dialog.show();
    }

    private void handleMapInRaceboardSettingsClick(ClickEvent e) {
        RaceMapSettings settings = new RaceMapSettings();
        RaceMapSettingsDialog dialog = new RaceMapSettingsDialog(settings, StringMessages.INSTANCE, 
                new DialogCallback<RaceMapSettings>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(RaceMapSettings newSettings) {
            }
        });
        dialog.show();
    }

//    @UiHandler("leaderboardAutoZoomBox")
//    public void onLeaderboardAutoZoomClicked(ValueChangeEvent<Boolean> ev) {
//        leaderboardZoomBox.setEnabled(!leaderboardAutoZoomBox.getValue());
//    }
    
    private String getLeaderboardZoom() {
        return leaderboardAutoZoomBox.getValue() == true ? "auto" : String.valueOf(leaderboardZoomBox.getValue());
    }

    private String getSelectedLocale() {
        String result = null;
        int selectedIndex = localeSelectionBox.getSelectedIndex();
        if(selectedIndex >= 0) {
            String selectedLocale = localeSelectionBox.getItemText(selectedIndex);
            for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
                if(!localeName.equals("default")) {
                    String displayName = LocaleInfo.getLocaleNativeDisplayName(localeName);
                    if(displayName.equals(selectedLocale)) {
                        result = localeName;
                        break;
                    }
                }
             }

        }
        return result;
    }

    private String getSelectedLeaderboardName() {
        String result = null;
        int selectedIndex = leaderboardSelectionBox.getSelectedIndex();
        if(selectedIndex > 0) {
            result = leaderboardSelectionBox.getItemText(selectedIndex);
        }
        return result;
    }

    private StrippedLeaderboardDTO getSelectedLeaderboard() {
        EventDTO selectedEvent = getSelectedEvent();
        String selectedLeaderboardName = getSelectedLeaderboardName();
        for (LeaderboardGroupDTO leaderboardGroup : selectedEvent.getLeaderboardGroups()) {
            for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                if (leaderboard.name.equals(selectedLeaderboardName)) {
                    return leaderboard;
                }
            }
        }
        return null;
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
