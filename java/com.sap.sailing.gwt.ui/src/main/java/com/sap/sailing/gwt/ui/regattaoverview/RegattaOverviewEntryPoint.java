package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;

public class RegattaOverviewEntryPoint extends AbstractEntryPoint  {
    
    private final static String PARAM_EVENT = "event";
    private final static String PARAM_ONLY_RUNNING_RACES = "onlyrunningraces";
    private final static String PARAM_ONLY_RACES_OF_SAME_DAY = "onlyracesofsameday";
    private final static String PARAM_REGATTA = "regatta";
    private final static String PARAM_COURSE_AREA = "coursearea";
    
    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();

        RootPanel rootPanel = RootPanel.get();
        boolean embedded = Window.Location.getParameter("embedded") != null
                && Window.Location.getParameter("embedded").equalsIgnoreCase("true");
        if (!embedded) {
            LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.regattaOverview(), stringMessages, this);
            logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
            rootPanel.add(logoAndTitlePanel);
        } else {
            RootPanel.getBodyElement().getStyle().setPadding(0, Unit.PX);
            RootPanel.getBodyElement().getStyle().setPaddingTop(20, Unit.PX);
        }
        
        String eventIdAsString = Window.Location.getParameter(PARAM_EVENT);
        
        RegattaRaceStatesSettings settings = createRegattaRaceStatesSettingsFromURL();
        
        RegattaOverviewPanel regattaOverviewPanel = new RegattaOverviewPanel(sailingService, this, stringMessages, eventIdAsString, settings);
        rootPanel.add(regattaOverviewPanel);
    }
    
    private RegattaRaceStatesSettings createRegattaRaceStatesSettingsFromURL() {
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
}