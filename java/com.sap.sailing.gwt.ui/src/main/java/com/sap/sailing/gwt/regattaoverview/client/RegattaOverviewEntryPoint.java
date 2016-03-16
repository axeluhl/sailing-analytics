package com.sap.sailing.gwt.regattaoverview.client;

import java.util.Collections;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.gwt.common.authentication.FixedSailingAuthentication;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.regattaoverview.client.RegattaRaceStatesComponent.EntryHandler;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;
import com.sap.sse.security.ui.authentication.generic.sapheader.SAPHeaderWithAuthentication;

public class RegattaOverviewEntryPoint extends AbstractSailingEntryPoint  {

    private final static String PARAM_EVENT = "event";
    private final static String PARAM_ONLY_RUNNING_RACES = "onlyrunningraces";
    private final static String PARAM_ONLY_RACES_OF_SAME_DAY = "onlyracesofsameday";
    private final static String PARAM_REGATTA = "regatta";
    private final static String PARAM_COURSE_AREA = "coursearea";
    private final static String PARAM_IGNORE_LOCAL_SETTINGS = "ignoreLocalSettings";
    
    private DockLayoutPanel containerPanel;
    private RaceDetailPanel detailPanel;
    private RegattaOverviewPanel regattaPanel;
    private final Label clockLabel = new Label();

    private final RegattaOverviewResources.LocalCss style = RegattaOverviewResources.INSTANCE.css();
    private SAPHeaderWithAuthentication siteHeader;

    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        SharedResources.INSTANCE.mediaCss().ensureInjected();
        SharedResources.INSTANCE.mainCss().ensureInjected();
        RegattaOverviewResources.INSTANCE.css().ensureInjected();

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        containerPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(containerPanel);
        containerPanel.addStyleName(RegattaOverviewResources.INSTANCE.css().container());
        
        clockLabel.addStyleName(style.clockLabel());

        siteHeader = new SAPHeaderWithAuthentication(getStringMessages()
                .sapSailingAnalytics());
        new FixedSailingAuthentication(getUserService(), siteHeader.getAuthenticationMenuView());

        siteHeader.addWidgetToRightSide(clockLabel);
        containerPanel.addNorth(siteHeader, 75);

        String eventIdAsString = Window.Location.getParameter(PARAM_EVENT);
        if (eventIdAsString == null) {
            Window.alert("Missing parameter");
            return;
        }

        createAndAddDetailPanel();
        createAndAddRegattaPanel(UUID.fromString(eventIdAsString));
        toggleDetailPanel(false);
        
        regattaPanel.setEntryClickedHandler(new EntryHandler() { 
            @Override
            public void onEntryClicked(RegattaOverviewEntryDTO entry) {
                detailPanel.show(entry);
                toggleDetailPanel(true);
            }

            @Override
            public void onEntryUpdated(RegattaOverviewEntryDTO entry) {
                detailPanel.update(entry);
            }
        });
    }
    
    private void toggleDetailPanel(boolean visibile) {
        containerPanel.setWidgetSize(detailPanel, visibile ? 110 : 0);
        containerPanel.animate(500);
        // containerPanel.setWidgetHidden(detailPanel, !visibile);
    }

    private void createAndAddRegattaPanel(UUID eventId) {
        RegattaRaceStatesSettings settings = createRegattaRaceStatesSettingsFromURL();
        boolean ignoreLocalSettings = getIgnoreLocalSettingsFromURL();
        regattaPanel = new RegattaOverviewPanel(sailingService, this, getStringMessages(), eventId, settings, userAgent, ignoreLocalSettings);

        regattaPanel.addHandler(new EventDTOLoadedEvent.Handler() {
            @Override
            public void onEventDTOLoaded(EventDTOLoadedEvent e) {
                siteHeader.setHeaderTitle(e.getCurrentEvent().getName());
                siteHeader.setHeaderSubTitle(e.getCurrentEvent().venue.getName());
            }
        }, EventDTOLoadedEvent.TYPE);

        regattaPanel.addHandler(new EventTimeUpdateEvent.Handler() {
            @Override
            public void onEventDTOLoaded(EventTimeUpdateEvent e) {
                clockLabel.setText(e.getFormattedUpdatedTime());
            }
        }, EventTimeUpdateEvent.TYPE);

        Panel centerPanel = new FlowPanel();
        centerPanel.add(regattaPanel);
        ScrollPanel scrollPanel = new ScrollPanel(centerPanel);
        containerPanel.add(scrollPanel);
    }

    private boolean getIgnoreLocalSettingsFromURL() {
        return Boolean.parseBoolean(Window.Location.getParameter(PARAM_IGNORE_LOCAL_SETTINGS));
    }

    private void createAndAddDetailPanel() {
        detailPanel = new RaceDetailPanel(getStringMessages(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggleDetailPanel(false);
            }
        });
        containerPanel.addSouth(detailPanel, 110);
    }

    public static RegattaRaceStatesSettings createRegattaRaceStatesSettingsFromURL() {
        return new SettingsToUrlSerializer().deserializeFromCurrentLocation(new RegattaRaceStatesSettings());
    }

    public static String getUrl(UUID eventId, RegattaRaceStatesSettings settings, boolean isSetVisibleCourseAreasInUrl,
            boolean isSetVisibleRegattasInUrl) {
        RegattaRaceStatesSettings copiedSettings = new RegattaRaceStatesSettings(settings.getVisibleCourseAreas(),
                settings.getVisibleRegattas(), settings.isShowOnlyRacesOfSameDay(),
                settings.isShowOnlyCurrentlyRunningRaces());
        if (!isSetVisibleCourseAreasInUrl) {
            copiedSettings.setVisibleCourseAreas(Collections.<UUID> emptyList());
        }
        if (!isSetVisibleRegattasInUrl) {
            copiedSettings.setVisibleRegattas(Collections.<String> emptyList());
        }
        return new SettingsToUrlSerializer().serializeUrlBuilderBasedOnCurrentLocationWithCleanParameters(copiedSettings)
                .setParameter(PARAM_IGNORE_LOCAL_SETTINGS, "true").setParameter(PARAM_EVENT, eventId.toString())
                .buildString();
    }
}