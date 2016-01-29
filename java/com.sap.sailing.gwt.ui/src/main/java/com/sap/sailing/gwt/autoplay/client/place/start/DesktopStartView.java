package com.sap.sailing.gwt.autoplay.client.place.start;

import java.util.ArrayList;
import java.util.List;

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
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerConfiguration;
import com.sap.sailing.gwt.autoplay.client.place.player.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.place.player.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeader;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderLifecycle;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.LifecycleSettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndSettings;

public class DesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, DesktopStartView> {
    }

    @UiField(provided=true) SAPHeader sapHeader;
    @UiField(provided=true) ListBox localeSelectionBox;
    @UiField(provided=true) ListBox eventSelectionBox;
    @UiField(provided=true) ListBox leaderboardSelectionBox;
    @UiField CheckBox startInFullscreenModeBox;
    @UiField Button startAutoPlayButton;
    @UiField DivElement leaderboardSelectionUi;
    @UiField DivElement screenConfigurationUi;
    @UiField FlowPanel leaderboardPerspectiveSettingsPanel;
    @UiField FlowPanel raceboardPerspectiveSettingsPanel;
    
    @UiField CheckBox autoSwitchToRaceboard;
    @UiField IntegerBox timeToRaceStartInSeconds;
    
    private final PlaceNavigator navigator;
    private final EventBus eventBus;
    private final List<EventDTO> events;

    private PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> leaderboardPerspectiveLifecycleAndSettings; 
    private PerspectiveLifecycleAndSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> raceboardPerspectiveLifecycleAndSettings; 

    private PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> leaderboardPerspectiveComponentLifecyclesAndSettings;
    private PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> raceboardPerspectiveComponentLifecyclesAndSettings;
    
    private final int defaultTimeToRaceStartTimeInSeconds = 180;
    
    public DesktopStartView(PlaceNavigator navigator, EventBus eventBus) {
        super();
        this.navigator = navigator;
        this.eventBus = eventBus;
        this.events = new ArrayList<EventDTO>();
        
        SAPHeaderLifecycle sapHeaderLifecycle = new SAPHeaderLifecycle(TextMessages.INSTANCE.autoPlayerConfiguration(), StringMessages.INSTANCE);
        sapHeader = new SAPHeader(sapHeaderLifecycle, sapHeaderLifecycle.createDefaultSettings(), false);
        eventSelectionBox = new ListBox();
        eventSelectionBox.setMultipleSelect(false);
        leaderboardSelectionBox = new ListBox();
        leaderboardSelectionBox.setMultipleSelect(false);
        localeSelectionBox = new ListBox();
        localeSelectionBox.setMultipleSelect(false);
        
        LocaleInfo currentLocale = LocaleInfo.getCurrentLocale();
        int i = 0;
        for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
            if (!localeName.equals("default")) {
                String displayName = LocaleInfo.getLocaleNativeDisplayName(localeName);
                localeSelectionBox.addItem(displayName);
                if (currentLocale.getLocaleName().equals(localeName)) {
                    localeSelectionBox.setSelectedIndex(i);
                }
                i++;
            }
        }

        initWidget(uiBinder.createAndBindUi(this));
        this.ensureDebugId("AutoPlayStartView");

        startInFullscreenModeBox.setValue(true);
        autoSwitchToRaceboard.setValue(true);
        timeToRaceStartInSeconds.setValue(defaultTimeToRaceStartTimeInSeconds);

        leaderboardSelectionUi.getStyle().setVisibility(Visibility.HIDDEN);
        screenConfigurationUi.getStyle().setVisibility(Visibility.HIDDEN);
        
        startAutoPlayButton.setEnabled(false);
        startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
    }

    private void updatePerspectives(AbstractLeaderboardDTO leaderboard) {
        LeaderboardWithHeaderPerspectiveLifecycle leaderboardPerspectiveLifecycle = new LeaderboardWithHeaderPerspectiveLifecycle(leaderboard, StringMessages.INSTANCE);
        LeaderboardWithHeaderPerspectiveSettings leaderboardPerspectiveSettings = leaderboardPerspectiveLifecycle.createDefaultSettings();
        
        CompositeLifecycleSettings leaderboardPerspectiveComponentsLifecyclesAndSettings = leaderboardPerspectiveLifecycle.getComponentLifecyclesAndDefaultSettings();
        leaderboardPerspectiveComponentLifecyclesAndSettings = new PerspectiveLifecycleAndComponentSettings<>(leaderboardPerspectiveLifecycle,
                leaderboardPerspectiveComponentsLifecyclesAndSettings);
        
        RaceBoardPerspectiveLifecycle raceboardPerspectiveLifecycle = new RaceBoardPerspectiveLifecycle(leaderboard, StringMessages.INSTANCE);
        RaceBoardPerspectiveSettings raceboardPerspectiveSettings = raceboardPerspectiveLifecycle.createDefaultSettings(); 

        CompositeLifecycleSettings raceboardPerspectiveComponentsLifecyclesAndSettings = raceboardPerspectiveLifecycle.getComponentLifecyclesAndDefaultSettings();
        
        raceboardPerspectiveComponentLifecyclesAndSettings = new PerspectiveLifecycleAndComponentSettings<>(raceboardPerspectiveLifecycle,
                raceboardPerspectiveComponentsLifecyclesAndSettings); 

        leaderboardPerspectiveLifecycleAndSettings = new PerspectiveLifecycleAndSettings<>(leaderboardPerspectiveLifecycle, leaderboardPerspectiveSettings);
        raceboardPerspectiveLifecycleAndSettings = new PerspectiveLifecycleAndSettings<>(raceboardPerspectiveLifecycle, raceboardPerspectiveSettings);
    }

    private <C extends Perspective<S>, S extends Settings> void openPerspectiveSettingsDialog(final PerspectiveLifecycleAndSettings<?,S> perspectiveLifecycleAndSettings) {
        SettingsDialogComponent<S> settingsDialogComponent = perspectiveLifecycleAndSettings.getPerspectiveLifecycle().getSettingsDialogComponent(perspectiveLifecycleAndSettings.getSettings());
        
        LifecycleSettingsDialog<S> dialog = new LifecycleSettingsDialog<S>(perspectiveLifecycleAndSettings.getPerspectiveLifecycle(), settingsDialogComponent, StringMessages.INSTANCE, new DialogCallback<S>() {
            @Override
            public void ok(S newSettings) {
                perspectiveLifecycleAndSettings.setSettings(newSettings);
            };
            @Override
            public void cancel() {
            }
        });
        dialog.show();
    }
    
    private void openPerspectiveComponentSettingsDialog(final PerspectiveLifecycle<?,?,?,?> perspectiveLifecycle,
            final PerspectiveLifecycleAndComponentSettings<?> perspectiveComponentsLifeyclesAndSettings) {
        TabbedPerspectiveConfigurationDialog dialog = new TabbedPerspectiveConfigurationDialog(StringMessages.INSTANCE,
                perspectiveLifecycle, perspectiveComponentsLifeyclesAndSettings.getComponentSettings(), new DialogCallback<CompositeLifecycleSettings>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(CompositeLifecycleSettings newSettings) {
                        perspectiveComponentsLifeyclesAndSettings.setComponentSettings(newSettings);
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

            createPerspectiveSettingsUI(leaderboardPerspectiveLifecycleAndSettings, leaderboardPerspectiveComponentLifecyclesAndSettings, leaderboardPerspectiveSettingsPanel);
            createPerspectiveSettingsUI(raceboardPerspectiveLifecycleAndSettings, raceboardPerspectiveComponentLifecyclesAndSettings, raceboardPerspectiveSettingsPanel);
        } else {
            startAutoPlayButton.setEnabled(false);
            startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        }
        screenConfigurationUi.getStyle().setVisibility(selectedLeaderboardName != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    private <C extends Perspective<S>, PLC extends PerspectiveLifecycle<?,S,?,?>, S extends Settings> void createPerspectiveSettingsUI(final PerspectiveLifecycleAndSettings<PLC,S> perspectiveLifecycleAndSettings,
            final PerspectiveLifecycleAndComponentSettings<?> perspectiveComponentsLifeyclesAndSettings,
            FlowPanel perspectiveSettingsPanel) { 
        perspectiveSettingsPanel.clear();

        Button perspectiveSettingsButton = new Button("Page settings");
        perspectiveSettingsButton.getElement().getStyle().setMarginRight(10, Unit.PX);
        perspectiveSettingsPanel.add(perspectiveSettingsButton);
        perspectiveSettingsButton.setEnabled(perspectiveLifecycleAndSettings.getPerspectiveLifecycle().hasSettings());
        perspectiveSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPerspectiveSettingsDialog(perspectiveLifecycleAndSettings);
            }
        });

        Button perspectiveComponentSettingsButton = new Button("Page component settings");
        perspectiveSettingsPanel.add(perspectiveComponentSettingsButton);
        perspectiveComponentSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPerspectiveComponentSettingsDialog((PerspectiveLifecycle<?, ?, ?, ?>) perspectiveLifecycleAndSettings.getPerspectiveLifecycle(),
                        perspectiveComponentsLifeyclesAndSettings);
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
        
        if(selectedEvent != null && selectedLeaderboardName != null) {
            navigator.goToPlayer(new AutoPlayerConfiguration(selectedEvent.id.toString(), selectedLeaderboardName,
                    startInFullscreenModeBox.getValue(), timeToRaceStartInSeconds.getValue()), 
                    leaderboardPerspectiveLifecycleAndSettings, raceboardPerspectiveLifecycleAndSettings,
                    leaderboardPerspectiveComponentLifecyclesAndSettings,
                    raceboardPerspectiveComponentLifecyclesAndSettings);
        }
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
