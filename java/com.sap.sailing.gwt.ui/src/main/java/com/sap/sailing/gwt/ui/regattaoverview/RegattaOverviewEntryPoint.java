package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.URLEncoder;
import com.sap.sailing.gwt.ui.regattaoverview.RegattaRaceStatesComponent.EntryHandler;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaOverviewEntryPoint extends AbstractEntryPoint  {

    private final static String PARAM_EVENT = "event";
    private final static String PARAM_ONLY_RUNNING_RACES = "onlyrunningraces";
    private final static String PARAM_ONLY_RACES_OF_SAME_DAY = "onlyracesofsameday";
    private final static String PARAM_REGATTA = "regatta";
    private final static String PARAM_COURSE_AREA = "coursearea";
    
    private DockLayoutPanel containerPanel;
    private RaceDetailPanel detailPanel;
    private RegattaOverviewPanel regattaPanel;

    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();

        RootLayoutPanel rootPanel = RootLayoutPanel.get();
        containerPanel = new DockLayoutPanel(Unit.PX);
        rootPanel.add(containerPanel);
        
        boolean embedded = Window.Location.getParameter("embedded") != null
                && Window.Location.getParameter("embedded").equalsIgnoreCase("true");
        if (!embedded) {
            LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.regattaOverview(), stringMessages, this);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            containerPanel.addNorth(logoAndTitlePanel, 68);
        } else {
            RootPanel.getBodyElement().getStyle().setPadding(0, Unit.PX);
            RootPanel.getBodyElement().getStyle().setPaddingTop(20, Unit.PX);
        }

        String eventIdAsString = Window.Location.getParameter(PARAM_EVENT);

        createAndAddDetailPanel();
        createAndAddRegattaPanel(eventIdAsString);
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

    private void createAndAddRegattaPanel(String eventIdAsString) {
        RegattaRaceStatesSettings settings = createRegattaRaceStatesSettingsFromURL();
        regattaPanel = new RegattaOverviewPanel(sailingService, this, stringMessages, eventIdAsString, settings);
        Panel centerPanel = new FlowPanel();
        centerPanel.add(regattaPanel);
        ScrollPanel scrollPanel = new ScrollPanel(centerPanel);
        containerPanel.add(scrollPanel);
    }

    private void createAndAddDetailPanel() {
        detailPanel = new RaceDetailPanel(stringMessages, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggleDetailPanel(false);
            }
        });
        containerPanel.addSouth(detailPanel, 120);
    }

    public static RegattaRaceStatesSettings createRegattaRaceStatesSettingsFromURL() {
        List<String> visibleCourseAreas = new ArrayList<String>();
        List<String> visibleRegattas = new ArrayList<String>();

        boolean showOnlyCurrentlyRunningRaces = Window.Location.getParameter(PARAM_ONLY_RUNNING_RACES) == null 
                || !Window.Location.getParameter(PARAM_ONLY_RUNNING_RACES).equalsIgnoreCase("false");

        boolean showOnlyRacesOfSameDay = Window.Location.getParameter(PARAM_ONLY_RACES_OF_SAME_DAY) != null 
                && Window.Location.getParameter(PARAM_ONLY_RACES_OF_SAME_DAY).equalsIgnoreCase("true");

        if (Window.Location.getParameterMap().containsKey(PARAM_COURSE_AREA)) {
            visibleCourseAreas.addAll(Window.Location.getParameterMap().get(PARAM_COURSE_AREA));
        }

        if (Window.Location.getParameterMap().containsKey(PARAM_REGATTA)) {
            visibleRegattas.addAll(Window.Location.getParameterMap().get(PARAM_REGATTA));
        }

        return new RegattaRaceStatesSettings(visibleCourseAreas, visibleRegattas, showOnlyRacesOfSameDay, showOnlyCurrentlyRunningRaces);
    }

    public static String getUrl(String eventIdAsString, RegattaRaceStatesSettings settings, 
            boolean isSetVisibleCourseAreasInUrl, boolean isSetVisibleRegattasInUrl) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        String showOnlyCurrentlyRunningRaces = "&" + PARAM_ONLY_RUNNING_RACES + "=" + (settings.isShowOnlyCurrentlyRunningRaces() ? "true" : "false");
        String showOnlyRacesOfSameDay = "&" + PARAM_ONLY_RACES_OF_SAME_DAY + "=" + (settings.isShowOnlyRacesOfSameDay() ? "true" : "false");

        StringBuilder visibleCourseAreas = new StringBuilder();
        if (isSetVisibleCourseAreasInUrl) {
            for (String visibleCourseArea : settings.getVisibleCourseAreas()) {
                visibleCourseAreas.append('&');
                visibleCourseAreas.append(PARAM_COURSE_AREA);
                visibleCourseAreas.append('=');
                visibleCourseAreas.append(visibleCourseArea);
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
        String link = URLEncoder.encode("/gwt/RegattaOverview.html?" + PARAM_EVENT+ "=" + eventIdAsString
                + visibleCourseAreas.toString()
                + visibleRegattas.toString()
                + showOnlyCurrentlyRunningRaces
                + showOnlyRacesOfSameDay
                + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
        return link;
    }
}