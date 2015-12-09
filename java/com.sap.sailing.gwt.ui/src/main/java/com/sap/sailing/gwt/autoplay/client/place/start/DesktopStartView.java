package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeader;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.AbstractCompetitorRaceChart;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartSettings;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartSettings;
import com.sap.sailing.gwt.ui.client.shared.perspective.Perspective;
import com.sap.sailing.gwt.ui.client.shared.perspective.TabbedPerspectiveConfigurationDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.leaderboard.ProxyLeaderboardPerspective;
import com.sap.sailing.gwt.ui.raceboard.ProxyRaceBoardPerspective;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;

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
    @UiField TextBox leaderboardZoomBox;
    @UiField Button startAutoPlayButton;
    @UiField DivElement leaderboardSelectionUi;
    @UiField DivElement leaderboardZoomDiv;
    @UiField DivElement leaderboardAutoZoomDiv;
    @UiField DivElement screenConfigurationUi;
    @UiField FlowPanel leaderboardPerspectiveSettingsPanel;
    @UiField FlowPanel raceboardPerspectiveSettingsPanel;
    
    @UiField CheckBox autoSwitchToRaceboard;
    @UiField TextBox timeToRaceStartInSeconds;
    
    private final PlaceNavigator navigator;
    private final EventBus eventBus;
    private final List<EventDTO> events;
    
    private ProxyLeaderboardPerspective leaderboardPerspective;
    private ProxyRaceBoardPerspective raceboardPerspective;
    
    private final int defaultTimeToStartTimeInSeconds = 180;
    private final Map<Perspective<?>, CompositeSettings> perspectiveSettings;
    
    public DesktopStartView(PlaceNavigator navigator, EventBus eventBus) {
        super();
        this.navigator = navigator;
        this.eventBus = eventBus;
        this.events = new ArrayList<EventDTO>();
        
        sapHeader = new SAPHeader("Auto player configuration", false);
        eventSelectionBox = new ListBox();
        eventSelectionBox.setMultipleSelect(false);
        leaderboardSelectionBox = new ListBox();
        leaderboardSelectionBox.setMultipleSelect(false);
        localeSelectionBox = new ListBox();
        localeSelectionBox.setMultipleSelect(false);

        perspectiveSettings = new HashMap<>();
        
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
        screenConfigurationUi.getStyle().setVisibility(Visibility.HIDDEN);
        
        startAutoPlayButton.setEnabled(false);
        startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
    }

    private void updatePerspectives(AbstractLeaderboardDTO leaderboard) {
        leaderboardPerspective = new ProxyLeaderboardPerspective(leaderboard, createDefaultLeaderboardSettings(leaderboard));
        raceboardPerspective = new ProxyRaceBoardPerspective(new RaceBoardPerspectiveSettings(), leaderboard, 
                createDefaultLeaderboardSettings(leaderboard), createDefaultMultiCompetitorRaceChartSettings());
        
        perspectiveSettings.clear();
    }

    private void openPerspectiveSettingsDialog(final Perspective<?> perspective) {
        SettingsDialog<?> dialog = new SettingsDialog<>(perspective, StringMessages.INSTANCE);
        dialog.show();
    }
    
    private void openPerspectiveComponentSettingsDialog(final Perspective<?> perspective) {
        TabbedPerspectiveConfigurationDialog dialog = new TabbedPerspectiveConfigurationDialog(StringMessages.INSTANCE,
                perspective, new DialogCallback<CompositeSettings>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(CompositeSettings newSettings) {
                        perspectiveSettings.put(perspective, newSettings);
                    }
                });
        dialog.show();
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
            this.updatePerspectives(selectedLeaderboard);

            createPerspectiveSettingsUI(leaderboardPerspective, leaderboardPerspectiveSettingsPanel);
            createPerspectiveSettingsUI(raceboardPerspective, raceboardPerspectiveSettingsPanel);
        } else {
            startAutoPlayButton.setEnabled(false);
            startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        }
        screenConfigurationUi.getStyle().setVisibility(selectedLeaderboardName != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    private void createPerspectiveSettingsUI(final Perspective<?> perspective, FlowPanel perspectiveSettingsPanel) {
        perspectiveSettingsPanel.clear();

        Button perspectiveSettingsButton = new Button("General Settings");
        perspectiveSettingsButton.getElement().getStyle().setMarginRight(10, Unit.PX);
        perspectiveSettingsPanel.add(perspectiveSettingsButton);
        perspectiveSettingsButton.setEnabled(perspective.hasSettings());
        perspectiveSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPerspectiveSettingsDialog(perspective);
            }
        });

        Button perspectiveComponentSettingsButton = new Button("Component settings");
        perspectiveSettingsPanel.add(perspectiveComponentSettingsButton);
        perspectiveComponentSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPerspectiveComponentSettingsDialog(perspective);
            }
        });
        
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
        
        if(selectedEvent != null && selectedLeaderboardName != null) {
            navigator.goToPlayer(new AutoPlayerConfiguration(selectedEvent.id.toString(), selectedLeaderboardName,
                    startInFullscreenModeBox.getValue(), leaderboardZoom));
        }
    }

    private MultiCompetitorRaceChartSettings createDefaultMultiCompetitorRaceChartSettings() {
        ChartSettings chartSettings = new ChartSettings(AbstractCompetitorRaceChart.DEFAULT_STEPSIZE);
        DetailType defaultDetailType = DetailType.WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD;
        return new MultiCompetitorRaceChartSettings(chartSettings, defaultDetailType);
    }
    
    private LeaderboardSettings createDefaultLeaderboardSettings(AbstractLeaderboardDTO leaderboard) {
        List<String> namesOfRaceColumnsToShow = new ArrayList<String>();
        for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
            namesOfRaceColumnsToShow.add(raceColumn.getName());
        }
        return LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                namesOfRaceColumnsToShow, /* namesOfRacesToShow */null, /* nameOfRaceToSort */null, /* autoExpandPreSelectedRace */
                false, /* showRegattaRank */ true, /*showCompetitorSailIdColumns*/ true,
                /*showCompetitorFullNameColumn*/ true);
    }
    
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
