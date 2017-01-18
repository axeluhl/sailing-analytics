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
import com.google.gwt.user.client.Window;
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
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveLifecycle;
import com.sap.sailing.gwt.autoplay.client.shared.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.GWTLocaleUtil;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialog;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;
import com.sap.sse.security.ui.client.UserService;

public class DesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, DesktopStartView> {
    }

    @UiField(provided=true) SAPSailingHeaderWithAuthentication sapHeader;
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
    
    private LeaderboardWithHeaderPerspectiveLifecycle leaderboardLifecycle;
    private PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> leaderboardSettings;
    private RaceBoardPerspectiveLifecycle raceboardLifecycle;
    private PerspectiveCompositeSettings<RaceBoardPerspectiveSettings> raceboardSettings;
    
    private final int defaultTimeToRaceStartTimeInSeconds = 180;
    
    public DesktopStartView(PlaceNavigator navigator, EventBus eventBus, UserService userService) {
        super();
        this.navigator = navigator;
        this.eventBus = eventBus;
        this.events = new ArrayList<EventDTO>();

        sapHeader = new SAPSailingHeaderWithAuthentication(StringMessages.INSTANCE.autoplayConfiguration());
        new FixedSailingAuthentication(userService, sapHeader.getAuthenticationMenuView());
        
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

        startInFullscreenModeBox.setValue(true);
        autoSwitchToRaceboard.setValue(true);
        timeToRaceStartInSeconds.setValue(defaultTimeToRaceStartTimeInSeconds);

        leaderboardSelectionUi.getStyle().setVisibility(Visibility.HIDDEN);
        screenConfigurationUi.getStyle().setVisibility(Visibility.HIDDEN);
        
        startAutoPlayButton.setEnabled(false);
        startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
    }

    private void updatePerspectives(AbstractLeaderboardDTO leaderboard) {
        leaderboardLifecycle = new LeaderboardWithHeaderPerspectiveLifecycle(leaderboard, StringMessages.INSTANCE);
        leaderboardSettings = leaderboardLifecycle.createDefaultSettings();
        raceboardLifecycle = new RaceBoardPerspectiveLifecycle(leaderboard, StringMessages.INSTANCE);
        raceboardSettings = raceboardLifecycle.createDefaultSettings();
    }

    private <PL extends PerspectiveLifecycle<PS>, PS extends Settings> void openPerspectiveSettingsDialog(
            final PL lifecycle, PerspectiveCompositeSettings<PS> settings) {
        PerspectiveCompositeLifecycleTabbedSettingsDialog<PL,PS> dialog = new PerspectiveCompositeLifecycleTabbedSettingsDialog<>(StringMessages.INSTANCE,
                lifecycle, settings, lifecycle.getLocalizedShortName(),
                new DialogCallback<PerspectiveCompositeSettings<PS>>() {
            @Override
            public void ok(PerspectiveCompositeSettings<PS> newSettings) {
                        // FIXME is callback required?
                        Window.alert("callback unused, prior called setAllSettings, determine what to do");
            };

            @Override
            public void cancel() {
            }
        });
        dialog.show();
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

            StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard();
            this.updatePerspectives(selectedLeaderboard);

            createPerspectiveSettingsUI(leaderboardLifecycle, leaderboardSettings, leaderboardPerspectiveSettingsPanel);
            createPerspectiveSettingsUI(raceboardLifecycle, raceboardSettings, raceboardPerspectiveSettingsPanel);
        } else {
            startAutoPlayButton.setEnabled(false);
            startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        }
        screenConfigurationUi.getStyle().setVisibility(selectedLeaderboardName != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    private <PL extends PerspectiveLifecycle<PS>, PS extends Settings> void createPerspectiveSettingsUI(
            final PL lifecycle, PerspectiveCompositeSettings<PS> settings,
            FlowPanel perspectiveSettingsPanel) { 
        perspectiveSettingsPanel.clear();

        Button perspectiveSettingsButton = new Button(StringMessages.INSTANCE.settings());
        perspectiveSettingsButton.getElement().getStyle().setMarginRight(10, Unit.PX);
        perspectiveSettingsPanel.add(perspectiveSettingsButton);
        perspectiveSettingsButton.setEnabled(settings.hasSettings());
        perspectiveSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPerspectiveSettingsDialog(lifecycle, settings);
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
                    leaderboardLifecycle, leaderboardSettings, raceboardLifecycle, raceboardSettings);
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
