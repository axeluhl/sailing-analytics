package com.sap.sailing.gwt.autoplay.client.places.config;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
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
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayConfiguration;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayContextDefinition;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayContextDefinitionImpl;
import com.sap.sailing.gwt.autoplay.client.configs.AutoPlayType;
import com.sap.sailing.gwt.autoplay.client.events.AutoPlayHeaderEvent;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.GWTLocaleUtil;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public class ConfigViewImpl extends Composite implements ConfigView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, ConfigViewImpl> {
    }

    @UiField(provided = true)
    ListBox localeSelectionBox;
    @UiField(provided = true)
    ListBox eventSelectionBox;
    @UiField(provided = true)
    ListBox configurationSelectionBox;
    @UiField(provided = true)
    ListBox leaderboardSelectionBox;
    @UiField
    Button startAutoPlayButton;
    @UiField
    Button settingsButton;
    @UiField
    DivElement leaderboardSelectionUi;
    private final EventBus eventBus;
    private final List<EventDTO> events;
    private AutoPlayClientFactory clientFactory;

    private AutoPlayConfiguration.Holder settingsHolder = new AutoPlayConfiguration.Holder();
    private AutoPlayType selectedAutoPlayType = null;
    private EventDTO selectedEvent;
    private StrippedLeaderboardDTO selectedLeaderboard;


    public ConfigViewImpl(AutoPlayClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
        this.eventBus = clientFactory.getEventBus();
        this.events = new ArrayList<EventDTO>();
        eventBus.fireEvent(new AutoPlayHeaderEvent(StringMessages.INSTANCE.autoplayConfiguration(), ""));
        eventSelectionBox = new ListBox();
        eventSelectionBox.setMultipleSelect(false);
        leaderboardSelectionBox = new ListBox();
        leaderboardSelectionBox.setMultipleSelect(false);
        localeSelectionBox = new ListBox();
        localeSelectionBox.setMultipleSelect(false);
        configurationSelectionBox = new ListBox();
        configurationSelectionBox.addItem("--", "");

        for (AutoPlayType apt : AutoPlayType.values()) {
            configurationSelectionBox.addItem(apt.getName(), apt.name());
        }
        configurationSelectionBox.addItem("", "");
        configurationSelectionBox.addItem("", "");
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

        validate();
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

    @UiHandler("configurationSelectionBox")
    void onConfigChange(ChangeEvent event) {
        String selectedConfigType = configurationSelectionBox.getSelectedValue();
        this.selectedAutoPlayType = AutoPlayType.valueOf(selectedConfigType);
        validate();
    }

    @UiHandler("eventSelectionBox")
    void onEventSelectionChange(ChangeEvent event) {
        this.selectedEvent = getSelectedEvent();
        if (this.selectedEvent != null) {
            leaderboardSelectionBox.clear();
            leaderboardSelectionBox.addItem(StringMessages.INSTANCE.selectALeaderboard());
            for (LeaderboardGroupDTO leaderboardGroup : selectedEvent.getLeaderboardGroups()) {
                for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                    leaderboardSelectionBox.addItem(leaderboard.name);
                }
            }
        }
        validate();
    }

    @UiHandler("leaderboardSelectionBox")
    void onLeaderboardSelectionChange(ChangeEvent event) {
        this.selectedLeaderboard = getSelectedLeaderboard();
        validate();
    }

    @UiHandler("settingsButton")
    void onOpenSettings(ClickEvent event) {
        selectedAutoPlayType.getConfig().loadSettings(selectedEvent, selectedLeaderboard, settingsHolder);
    }

    private boolean validate() {
        boolean readyToGo = true;
        if (readyToGo && selectedAutoPlayType == null) {
            readyToGo = false;
            eventSelectionBox.setEnabled(false);
            eventSelectionBox.getElement().getStyle().setOpacity(0.2);
        } else {
            eventSelectionBox.setEnabled(true);
            eventSelectionBox.getElement().getStyle().setOpacity(1);
        }

        EventDTO selectedEvent = getSelectedEvent();
        if (selectedEvent == null) {
            readyToGo = false;
            leaderboardSelectionBox.setEnabled(false);
            leaderboardSelectionBox.getElement().getStyle().setOpacity(0.2);
        } else {
            leaderboardSelectionBox.setEnabled(true);
            leaderboardSelectionBox.getElement().getStyle().setOpacity(1);
        }

        String selectedLeaderboardName = getSelectedLeaderboardName();
        if (readyToGo && selectedLeaderboardName == null)
            readyToGo = false;

        startAutoPlayButton.setEnabled(readyToGo);
        if (readyToGo) {
            startAutoPlayButton.removeStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
            settingsButton.removeStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        } else {
            startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
            settingsButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        }
        return readyToGo;
    }

    @UiHandler("localeSelectionBox")
    void onLocaleSelectionChange(ChangeEvent event) {
        String selectedLocale = getSelectedLocale();
        LocaleChangeEvent localeChangeEvent = new LocaleChangeEvent(selectedLocale);
        eventBus.fireEvent(localeChangeEvent);
    }

    @UiHandler("startAutoPlayButton")
    void startAutoPlayClicked(ClickEvent event) {
        if (validate()) {
            EventDTO selectedEvent = getSelectedEvent();
            String selectedLeaderboardName = getSelectedLeaderboardName();
            AutoPlayContextDefinition apcd = new AutoPlayContextDefinitionImpl(selectedAutoPlayType, selectedEvent.id, selectedLeaderboardName);
            selectedAutoPlayType.getConfig().startRootNode(clientFactory, apcd, settingsHolder.getSettings());
            
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
        if (events != null && selectedIndex > 0) {
            String selectedItemText = eventSelectionBox.getItemText(selectedIndex);
            for (EventDTO event : events) {
                if (event.getName().equals(selectedItemText)) {
                    result = event;
                    break;
                }
            }
        }
        return result;
    }

    interface OnSettingsCallback<PSS extends Settings> {
        void newSettings(PerspectiveCompositeSettings<PSS> newSettings);
    }


}
