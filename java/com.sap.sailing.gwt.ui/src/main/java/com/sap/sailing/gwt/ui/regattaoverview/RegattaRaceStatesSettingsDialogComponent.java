package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;

public class RegattaRaceStatesSettingsDialogComponent implements SettingsDialogComponent<RegattaRaceStatesSettings> {

    private final StringMessages stringMessages;
    private final RegattaRaceStatesSettings initialSettings;
    private final String eventIdAsString;
    private final List<CourseAreaDTO> courseAreas;
    private final List<RaceGroupDTO> raceGroups;

    private CheckBox showOnlyRacesOfSameDayCheckBox;
    private CheckBox showOnlyCurrentlyRunningRacesCheckBox;
    private final Map<String, CheckBox> courseAreaCheckBoxMap;
    private final Map<String, CheckBox> regattaCheckBoxMap;
    private final Anchor resultingLink;
    
    private final static String SETTINGS_DIALOG_COMPONENT = "SettingsDialogComponent";
    
    public RegattaRaceStatesSettingsDialogComponent(RegattaRaceStatesSettings settings, StringMessages stringMessages, 
            String eventIdAsString, List<CourseAreaDTO> courseAreas, List<RaceGroupDTO> raceGroups) {
        this.stringMessages = stringMessages;
        this.initialSettings = settings;
        this.eventIdAsString = eventIdAsString;
        this.courseAreas = courseAreas;
        this.raceGroups = raceGroups;
        this.courseAreaCheckBoxMap = new HashMap<String, CheckBox>();
        this.regattaCheckBoxMap = new HashMap<String, CheckBox>();
        this.resultingLink = new Anchor(stringMessages.asLink());
    }

    private FlowPanel fillCourseAreaWidget(DataEntryDialog<?> dialog) {
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.addStyleName(SETTINGS_DIALOG_COMPONENT);
        flowPanel.add(dialog.createHeadline(stringMessages.showFollowingCourseAreas(), true));
        FlowPanel courseAreaPanel = new FlowPanel();
        flowPanel.add(courseAreaPanel);
        
        int maxCourseAreasPerRow = 4;
        int numberOfCourseAreas = courseAreas.size();
        int numberOfRequiredRows = numberOfCourseAreas / maxCourseAreasPerRow;
        if (numberOfCourseAreas % maxCourseAreasPerRow != 0) {
            numberOfRequiredRows++;
        }
        int rowIndex = 0;
        int columnIndex = 0;
        
        Grid courseAreaGrid = new Grid(numberOfRequiredRows, maxCourseAreasPerRow);
        courseAreaPanel.add(courseAreaGrid);
        
        for (CourseAreaDTO courseAreaDTO : courseAreas) {
            CheckBox checkBox = dialog.createCheckbox(courseAreaDTO.getName());
            checkBox.setValue(Util.contains(initialSettings.getVisibleCourseAreas(), courseAreaDTO.id));
            courseAreaCheckBoxMap.put(courseAreaDTO.id, checkBox);
            
            courseAreaGrid.setWidget(rowIndex, columnIndex++, checkBox);
            if(columnIndex == maxCourseAreasPerRow) {
                rowIndex++;
                columnIndex = 0;
            }
        }
        return flowPanel;
    }
    
    private FlowPanel fillRegattaNamesWidget(DataEntryDialog<?> dialog) {
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.addStyleName(SETTINGS_DIALOG_COMPONENT);
        flowPanel.add(dialog.createHeadline(stringMessages.showFollowingRegattas(), true));
        FlowPanel regattaNamesPanel = new FlowPanel();
        flowPanel.add(regattaNamesPanel);
        
        int maxRegattasPerRow = 4;
        int numberOfRegattas = raceGroups.size();
        int numberOfRequiredRows = numberOfRegattas / maxRegattasPerRow;
        if (numberOfRegattas % maxRegattasPerRow != 0) {
            numberOfRequiredRows++;
        }
        int rowIndex = 0;
        int columnIndex = 0;
        
        Grid regattaGrid = new Grid(numberOfRequiredRows, maxRegattasPerRow);
        regattaNamesPanel.add(regattaGrid);
        
        for (RaceGroupDTO raceGroup : raceGroups) {
            CheckBox checkBox = dialog.createCheckbox(raceGroup.displayName);
            checkBox.setValue(Util.contains(initialSettings.getVisibleRegattas(), raceGroup.getName()));
            regattaCheckBoxMap.put(raceGroup.getName(), checkBox);
            
            regattaGrid.setWidget(rowIndex, columnIndex++, checkBox);
            if(columnIndex == maxRegattasPerRow) {
                rowIndex++;
                columnIndex = 0;
            }
        }
        return flowPanel;
    }
    
    private CheckBox getShowOnlyRacesOfSameDayWidget(DataEntryDialog<?> dialog) {
        showOnlyRacesOfSameDayCheckBox = dialog.createCheckbox(stringMessages.showOnlyRacesOfSameDay());
        showOnlyRacesOfSameDayCheckBox.setValue(initialSettings.isShowOnlyRacesOfSameDay());
        return showOnlyRacesOfSameDayCheckBox;
    }
    
    private CheckBox getShowOnlyCurrentlyRunningRacesWidget(DataEntryDialog<?> dialog) {
        showOnlyCurrentlyRunningRacesCheckBox = dialog.createCheckbox(stringMessages.showOnlyCurrentlyRunningRaces());
        showOnlyCurrentlyRunningRacesCheckBox.setValue(initialSettings.isShowOnlyCurrentlyRunningRaces());
        return showOnlyCurrentlyRunningRacesCheckBox;
    }
    
    private Widget getAdditionalSettingsWidget(DataEntryDialog<?> dialog) {
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.addStyleName(SETTINGS_DIALOG_COMPONENT);
        flowPanel.add(dialog.createHeadline(stringMessages.additionalSettings(), true));
        FlowPanel additionalSettingsPanel = new FlowPanel();
        flowPanel.add(additionalSettingsPanel);
        
        additionalSettingsPanel.add(getShowOnlyRacesOfSameDayWidget(dialog));
        additionalSettingsPanel.add(getShowOnlyCurrentlyRunningRacesWidget(dialog));

        return flowPanel;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel verticalPanel = new VerticalPanel();
        
        verticalPanel.add(fillCourseAreaWidget(dialog));
        verticalPanel.add(fillRegattaNamesWidget(dialog));
        verticalPanel.add(getAdditionalSettingsWidget(dialog));
        verticalPanel.add(resultingLink);
        
        return verticalPanel;
    }

    @Override
    public RegattaRaceStatesSettings getResult() {
        List<String> selectedCourseAreas = new ArrayList<String>();
        for (Entry<String, CheckBox> entry : courseAreaCheckBoxMap.entrySet()) {
            if (entry.getValue().getValue()) {
                selectedCourseAreas.add(entry.getKey());
            }
        }
        
        List<String> selectedRegattas = new ArrayList<String>();
        for (Entry<String, CheckBox> entry : regattaCheckBoxMap.entrySet()) {
            if (entry.getValue().getValue()) {
                selectedRegattas.add(entry.getKey());
            }
        }
        
        boolean isShowOnlyRacesOfSameDay = showOnlyRacesOfSameDayCheckBox.getValue();
        boolean isShowOnlyCurrentlyRunningRaces = showOnlyCurrentlyRunningRacesCheckBox.getValue();
        return new RegattaRaceStatesSettings(selectedCourseAreas, selectedRegattas, isShowOnlyRacesOfSameDay, isShowOnlyCurrentlyRunningRaces);
    }

    @Override
    public Validator<RegattaRaceStatesSettings> getValidator() {
        return new Validator<RegattaRaceStatesSettings>() {
            @Override
            public String getErrorMessage(RegattaRaceStatesSettings settings) {
                String errorMessage = null;
                if (errorMessage == null) {
                    updateLinkUrl(eventIdAsString, settings);
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return null;
    }
    
    private void updateLinkUrl(String eventIdAsString, RegattaRaceStatesSettings settings) {
        boolean isSetVisibleCourseAreasInUrl = true;
        boolean isSetVisibleRegattasInUrl = true;
        if (settings.getVisibleCourseAreas().size() == courseAreas.size()) {
            isSetVisibleCourseAreasInUrl = false;
        }
        if (settings.getVisibleRegattas().size() == raceGroups.size()) {
            isSetVisibleRegattasInUrl = false;
        }
        resultingLink.setHref(RegattaOverviewEntryPoint.getUrl(eventIdAsString, settings, isSetVisibleCourseAreasInUrl, isSetVisibleRegattasInUrl));
    }

}
