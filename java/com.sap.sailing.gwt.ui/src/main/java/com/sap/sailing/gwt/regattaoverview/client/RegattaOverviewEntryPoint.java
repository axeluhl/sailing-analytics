package com.sap.sailing.gwt.regattaoverview.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.gwt.regattaoverview.client.RegattaRaceStatesComponent.EntryHandler;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sse.gwt.client.URLEncoder;
import com.sap.sse.gwt.theme.client.component.sapheader.SAPHeader;
import com.sap.sse.gwt.theme.client.resources.ThemeResources;

public class RegattaOverviewEntryPoint extends AbstractSailingEntryPoint  {

    private final static String PARAM_EVENT = "event";
    private final static String PARAM_ONLY_RUNNING_RACES = "onlyrunningraces";
    private final static String PARAM_ONLY_RACES_OF_SAME_DAY = "onlyracesofsameday";
    private final static String PARAM_REGATTA = "regatta";
    private final static String PARAM_COURSE_AREA = "coursearea";
    
    private HeaderPanel headerPanel;
    private DockLayoutPanel containerPanel;
    private RaceDetailPanel detailPanel;
    private RegattaOverviewPanel regattaPanel;

    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        
        ThemeResources.INSTANCE.mediaCss().ensureInjected();
        ThemeResources.INSTANCE.mainCss().ensureInjected();
        RegattaOverviewResources.INSTANCE.css().ensureInjected();

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        
        headerPanel = new HeaderPanel();
        rootPanel.add(headerPanel);
        
        containerPanel = new DockLayoutPanel(Unit.PX);
        headerPanel.setContentWidget(containerPanel);
        
        boolean embedded = Window.Location.getParameter("embedded") != null
                && Window.Location.getParameter("embedded").equalsIgnoreCase("true");
        if (!embedded) {

            SAPHeader logoAndTitlePanel = new SAPHeader(getStringMessages().eventOverview(), "", false);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            headerPanel.setHeaderWidget(logoAndTitlePanel);
        } else {
            RootPanel.getBodyElement().getStyle().setPadding(0, Unit.PX);
            RootPanel.getBodyElement().getStyle().setPaddingTop(20, Unit.PX);
        }

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
        containerPanel.setWidgetHidden(detailPanel, !visibile);
    }

    private void createAndAddRegattaPanel(UUID eventId) {
        RegattaRaceStatesSettings settings = createRegattaRaceStatesSettingsFromURL();
        regattaPanel = new RegattaOverviewPanel(sailingService, this, getStringMessages(), eventId, settings, userAgent);
        Panel centerPanel = new FlowPanel();
        centerPanel.add(regattaPanel);
        ScrollPanel scrollPanel = new ScrollPanel(centerPanel);
        containerPanel.add(scrollPanel);
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
        List<UUID> visibleCourseAreas = new ArrayList<UUID>();
        List<String> visibleRegattas = new ArrayList<String>();

        boolean showOnlyCurrentlyRunningRaces = Window.Location.getParameter(PARAM_ONLY_RUNNING_RACES) == null 
                || !Window.Location.getParameter(PARAM_ONLY_RUNNING_RACES).equalsIgnoreCase("false");

        boolean showOnlyRacesOfSameDay = Window.Location.getParameter(PARAM_ONLY_RACES_OF_SAME_DAY) != null 
                && Window.Location.getParameter(PARAM_ONLY_RACES_OF_SAME_DAY).equalsIgnoreCase("true");

        if (Window.Location.getParameterMap().containsKey(PARAM_COURSE_AREA)) {
            for (String value : Window.Location.getParameterMap().get(PARAM_COURSE_AREA)) {
                visibleCourseAreas.add(UUID.fromString(value));
            }
        }

        if (Window.Location.getParameterMap().containsKey(PARAM_REGATTA)) {
            visibleRegattas.addAll(Window.Location.getParameterMap().get(PARAM_REGATTA));
        }

        return new RegattaRaceStatesSettings(visibleCourseAreas, visibleRegattas, showOnlyRacesOfSameDay, showOnlyCurrentlyRunningRaces);
    }

    public static String getUrl(UUID eventId, RegattaRaceStatesSettings settings, 
            boolean isSetVisibleCourseAreasInUrl, boolean isSetVisibleRegattasInUrl) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        String showOnlyCurrentlyRunningRaces = "&" + PARAM_ONLY_RUNNING_RACES + "=" + (settings.isShowOnlyCurrentlyRunningRaces() ? "true" : "false");
        String showOnlyRacesOfSameDay = "&" + PARAM_ONLY_RACES_OF_SAME_DAY + "=" + (settings.isShowOnlyRacesOfSameDay() ? "true" : "false");

        StringBuilder visibleCourseAreas = new StringBuilder();
        if (isSetVisibleCourseAreasInUrl) {
            for (UUID visibleCourseArea : settings.getVisibleCourseAreas()) {
                visibleCourseAreas.append('&');
                visibleCourseAreas.append(PARAM_COURSE_AREA);
                visibleCourseAreas.append('=');
                visibleCourseAreas.append(visibleCourseArea.toString());
            }
        }
        StringBuilder visibleRegattas = new StringBuilder();
        if (isSetVisibleRegattasInUrl) {
            for (String visibleRegatta : settings.getVisibleRegattas()) {
                visibleRegattas.append('&');
                visibleRegattas.append(PARAM_REGATTA);
                visibleRegattas.append('=');
                visibleRegattas.append(visibleRegatta);
            }
        }
        String link = URLEncoder.encode("/gwt/RegattaOverview.html?" + PARAM_EVENT+ "=" + eventId.toString()
                + visibleCourseAreas.toString()
                + visibleRegattas.toString()
                + showOnlyCurrentlyRunningRaces
                + showOnlyRacesOfSameDay
                + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
        return link;
    }
}