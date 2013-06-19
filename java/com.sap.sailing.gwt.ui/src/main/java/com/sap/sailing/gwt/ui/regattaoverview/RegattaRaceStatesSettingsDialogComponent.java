package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

public class RegattaRaceStatesSettingsDialogComponent implements SettingsDialogComponent<RegattaRaceStatesSettings> {

    private final StringMessages stringMessages;
    private final RegattaRaceStatesSettings initialSettings;
    private final List<CourseAreaDTO> courseAreas;
    private final List<String> regattaNames;

    private CheckBox showOnlyRacesOfSameDayCheckBox;
    private CheckBox showOnlyCurrentlyRunningRacesCheckBox;
    private final Map<String, CheckBox> courseAreaCheckBoxMap;
    private final Map<String, CheckBox> regattaCheckBoxMap;
    
    private final static String SETTINGS_DIALOG_COMPONENT = "SettingsDialogComponent";
    
    public RegattaRaceStatesSettingsDialogComponent(RegattaRaceStatesSettings settings, StringMessages stringMessages, 
            List<CourseAreaDTO> courseAreas, List<String> regattaNames) {
        this.stringMessages = stringMessages;
        this.initialSettings = settings;
        this.courseAreas = courseAreas;
        this.regattaNames = regattaNames;
        this.courseAreaCheckBoxMap = new HashMap<String, CheckBox>();
        this.regattaCheckBoxMap = new HashMap<String, CheckBox>();
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
        int numberOfRegattas = regattaNames.size();
        int numberOfRequiredRows = numberOfRegattas / maxRegattasPerRow;
        if (numberOfRegattas % maxRegattasPerRow != 0) {
            numberOfRequiredRows++;
        }
        int rowIndex = 0;
        int columnIndex = 0;
        
        Grid regattaGrid = new Grid(numberOfRequiredRows, maxRegattasPerRow);
        regattaNamesPanel.add(regattaGrid);
        
        for (String regattaName : regattaNames) {
            CheckBox checkBox = dialog.createCheckbox(regattaName);
            checkBox.setValue(Util.contains(initialSettings.getVisibleRegattas(), regattaName));
            regattaCheckBoxMap.put(regattaName, checkBox);
            
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
        VerticalPanel vp = new VerticalPanel();
        
        vp.add(fillCourseAreaWidget(dialog));
        vp.add(fillRegattaNamesWidget(dialog));
        vp.add(getAdditionalSettingsWidget(dialog));
        
        return vp;
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
            public String getErrorMessage(RegattaRaceStatesSettings valueToValidate) {
                String errorMessage = null;
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return null;
    }

}
