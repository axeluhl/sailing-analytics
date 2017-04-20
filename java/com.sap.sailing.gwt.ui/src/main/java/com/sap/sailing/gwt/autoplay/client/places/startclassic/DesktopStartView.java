package com.sap.sailing.gwt.autoplay.client.places.startclassic;

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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.app.PlaceNavigator;
import com.sap.sailing.gwt.autoplay.client.place.player.AutoPlayerContextDefinition;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.authentication.SAPSailingHeaderWithAuthentication;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.GWTLocaleUtil;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.event.LocaleChangeEvent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogForLinkSharing;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;
import com.sap.sse.security.ui.client.UserService;

public class DesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, DesktopStartView> {
    }

    @UiField(provided = true)
    SAPSailingHeaderWithAuthentication sapHeader;
    @UiField(provided = true)
    ListBox localeSelectionBox;
    @UiField(provided = true)
    ListBox eventSelectionBox;
    @UiField(provided = true)
    ListBox leaderboardSelectionBox;
    @UiField
    Button startAutoPlayButton;
    @UiField
    DivElement leaderboardSelectionUi;
    @UiField
    DivElement screenConfigurationUi;
    @UiField
    FlowPanel leaderboardPerspectiveSettingsPanel;

    private final PlaceNavigator navigator;
    private final EventBus eventBus;
    private final List<EventDTO> events;

    private AutoplayPerspectiveLifecycle autoplayLifecycle;
    protected PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> autoplayPerspectiveSettings;

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

        leaderboardSelectionUi.getStyle().setVisibility(Visibility.HIDDEN);
        screenConfigurationUi.getStyle().setVisibility(Visibility.HIDDEN);

        startAutoPlayButton.setEnabled(false);
        startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
    }

    private void updatePerspectives(AbstractLeaderboardDTO leaderboard) {
        autoplayLifecycle = new AutoplayPerspectiveLifecycle(leaderboard);
        autoplayPerspectiveSettings = autoplayLifecycle.createDefaultSettings();
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
        if (selectedEvent != null) {
            leaderboardSelectionBox.clear();
            leaderboardSelectionBox.addItem(StringMessages.INSTANCE.selectALeaderboard());
            for (LeaderboardGroupDTO leaderboardGroup : selectedEvent.getLeaderboardGroups()) {
                for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                    leaderboardSelectionBox.addItem(leaderboard.name);
                }
            }
        }
        leaderboardSelectionUi.getStyle().setVisibility(selectedEvent != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    @UiHandler("leaderboardSelectionBox")
    void onLeaderboardSelectionChange(ChangeEvent event) {
        String selectedLeaderboardName = getSelectedLeaderboardName();
        if (selectedLeaderboardName != null) {
            startAutoPlayButton.setEnabled(true);
            startAutoPlayButton.removeStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());

            StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard();
            this.updatePerspectives(selectedLeaderboard);

            leaderboardPerspectiveSettingsPanel.clear();
            Button perspectiveSettingsButton = new Button(StringMessages.INSTANCE.settings());
            perspectiveSettingsButton.getElement().getStyle().setMarginRight(10, Unit.PX);
            leaderboardPerspectiveSettingsPanel.add(perspectiveSettingsButton);
            perspectiveSettingsButton.setEnabled(true);
            perspectiveSettingsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    openSettingsDialog();
                }
            });
        } else {
            startAutoPlayButton.setEnabled(false);
            startAutoPlayButton.addStyleName(SharedResources.INSTANCE.mainCss().buttoninactive());
        }
        screenConfigurationUi.getStyle()
                .setVisibility(selectedLeaderboardName != null ? Visibility.VISIBLE : Visibility.HIDDEN);
    }

    protected void openSettingsDialog() {
        DialogCallback<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>> callback = new DialogCallback<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>>() {

            @Override
            public void ok(PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings> editedObject) {
                autoplayPerspectiveSettings = editedObject;
            }

            @Override
            public void cancel() {
            }
        };

        if (autoplayPerspectiveSettings == null) {
            autoplayPerspectiveSettings = autoplayLifecycle.createDefaultSettings();
        }

        new SettingsDialogForLinkSharing<PerspectiveCompositeSettings<AutoplayPerspectiveOwnSettings>>(null,
                autoplayLifecycle, autoplayPerspectiveSettings, StringMessages.INSTANCE, true, callback).show();

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

        if (selectedEvent != null && selectedLeaderboardName != null) {
            // TODO generate place settings url, and directly start other place
            String contextAndSettings = new SettingsToStringSerializer().fromSettings(
                    new AutoPlayerContextDefinition(selectedEvent.id, selectedLeaderboardName), autoplayPerspectiveSettings);
            navigator.goToPlayer(contextAndSettings);
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
