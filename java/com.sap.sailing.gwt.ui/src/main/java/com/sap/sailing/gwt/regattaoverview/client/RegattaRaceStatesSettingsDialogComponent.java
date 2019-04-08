package com.sap.sailing.gwt.regattaoverview.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class RegattaRaceStatesSettingsDialogComponent implements SettingsDialogComponent<RegattaRaceStatesSettings> {

    private final StringMessages stringMessages;
    private final RegattaRaceStatesSettings initialSettings;
    private final List<CourseAreaDTO> courseAreas;
    private final List<RaceGroupDTO> raceGroups;

    private CheckBox showOnlyRacesOfSameDayCheckBox;
    private CheckBox showOnlyCurrentlyRunningRacesCheckBox;
    private final Map<UUID, CheckBox> courseAreaCheckBoxMap;
    private final Map<String, CheckBox> regattaCheckBoxMap;
    private Button courseAreaDeselectButton;
    private Button regattaDeselectButton;
    
    private final static String SETTINGS_DIALOG_COMPONENT = "SettingsDialogComponent";
    
    public RegattaRaceStatesSettingsDialogComponent(RegattaRaceStatesSettings settings, StringMessages stringMessages, 
            List<CourseAreaDTO> courseAreas, List<RaceGroupDTO> raceGroups) {
        this.stringMessages = stringMessages;
        this.initialSettings = settings;
        this.courseAreas = courseAreas;
        this.raceGroups = raceGroups;
        this.courseAreaCheckBoxMap = new HashMap<UUID, CheckBox>();
        this.regattaCheckBoxMap = new HashMap<String, CheckBox>();
    }

    private FlowPanel fillCourseAreaWidget(DataEntryDialog<?> dialog) {
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.ensureDebugId("CourseAreaPanel");
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
        
        boolean allCheckboxesSelected = true;
        for (CourseAreaDTO courseAreaDTO : courseAreas) {
            CheckBox checkBox = dialog.createCheckbox(courseAreaDTO.getName());
            boolean isCourseAreaVisible = Util.contains(initialSettings.getVisibleCourseAreas(), courseAreaDTO.id);
            allCheckboxesSelected &= isCourseAreaVisible;
            checkBox.setValue(isCourseAreaVisible);
            courseAreaCheckBoxMap.put(courseAreaDTO.id, checkBox);
            
            courseAreaGrid.setWidget(rowIndex, columnIndex++, checkBox);
            if(columnIndex == maxCourseAreasPerRow) {
                rowIndex++;
                columnIndex = 0;
            }
        }
        
        courseAreaDeselectButton = new Button();
        setTextOfDeselectButton(courseAreaDeselectButton, allCheckboxesSelected);
        courseAreaDeselectButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (courseAreaDeselectButton.getText().equals(stringMessages.deselectAll())) {
                    for (CheckBox checkBox : courseAreaCheckBoxMap.values()) {
                        checkBox.setValue(false);
                    }
                    courseAreaDeselectButton.setText(stringMessages.selectAll());
                } else {
                    for (CheckBox checkBox : courseAreaCheckBoxMap.values()) {
                        checkBox.setValue(true);
                    }
                    courseAreaDeselectButton.setText(stringMessages.deselectAll());
                }
            }
            
        });
        flowPanel.add(courseAreaDeselectButton);
        
        return flowPanel;
    }
    
    private FlowPanel fillRegattaNamesWidget(DataEntryDialog<?> dialog) {
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.ensureDebugId("RegattaNamesPanel");
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
        
        boolean allCheckboxesSelected = true;
        for (RaceGroupDTO raceGroup : raceGroups) {
            CheckBox checkBox = dialog.createCheckbox(raceGroup.displayName);
            boolean isRaceGroupVisible = Util.contains(initialSettings.getVisibleRegattas(), raceGroup.getName());
            allCheckboxesSelected &= isRaceGroupVisible;
            checkBox.setValue(isRaceGroupVisible);
            regattaCheckBoxMap.put(raceGroup.getName(), checkBox);
            
            regattaGrid.setWidget(rowIndex, columnIndex++, checkBox);
            if(columnIndex == maxRegattasPerRow) {
                rowIndex++;
                columnIndex = 0;
            }
        }
        
        regattaDeselectButton = new Button();
        setTextOfDeselectButton(regattaDeselectButton, allCheckboxesSelected);
        regattaDeselectButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (regattaDeselectButton.getText().equals(stringMessages.deselectAll())) {
                    for (CheckBox checkBox : regattaCheckBoxMap.values()) {
                        checkBox.setValue(false);
                    }
                    regattaDeselectButton.setText(stringMessages.selectAll());
                } else {
                    for (CheckBox checkBox : regattaCheckBoxMap.values()) {
                        checkBox.setValue(true);
                    }
                    regattaDeselectButton.setText(stringMessages.deselectAll());
                }
            }
            
        });
        flowPanel.add(regattaDeselectButton);
        
        return flowPanel;
    }
    
    private void setTextOfDeselectButton(Button deselectButton, boolean allCheckboxesSelected) {
        if (allCheckboxesSelected) {
            deselectButton.setText(stringMessages.deselectAll());
        } else {
            deselectButton.setText(stringMessages.selectAll());
        }
    }
    
    private CheckBox getShowOnlyRacesOfSameDayWidget(DataEntryDialog<?> dialog) {
        showOnlyRacesOfSameDayCheckBox = dialog.createCheckbox(stringMessages.showOnlyRacesOfSameDay());
        showOnlyRacesOfSameDayCheckBox.ensureDebugId("ShowOnlyRacesOfSameDayCheckBox");
        showOnlyRacesOfSameDayCheckBox.setValue(initialSettings.isShowOnlyRacesOfSameDay());
        return showOnlyRacesOfSameDayCheckBox;
    }
    
    private CheckBox getShowOnlyCurrentlyRunningRacesWidget(DataEntryDialog<?> dialog) {
        showOnlyCurrentlyRunningRacesCheckBox = dialog.createCheckbox(stringMessages.showOnlyCurrentlyRunningRaces());
        showOnlyCurrentlyRunningRacesCheckBox.ensureDebugId("ShowOnlyCurrentlyRunningRacesCheckBox");
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
        dialog.ensureDebugId("RegattaRacesStatesSettingsDialog");
        
        verticalPanel.add(fillCourseAreaWidget(dialog));
        verticalPanel.add(fillRegattaNamesWidget(dialog));
        verticalPanel.add(getAdditionalSettingsWidget(dialog));
        
        return verticalPanel;
    }

    @Override
    public RegattaRaceStatesSettings getResult() {
        List<UUID> selectedCourseAreas = new ArrayList<UUID>();
        for (Entry<UUID, CheckBox> entry : courseAreaCheckBoxMap.entrySet()) {
            if (entry.getValue().getValue()) {
                selectedCourseAreas.add(entry.getKey());
            }
        }
        boolean allCourseAreasSelected = selectedCourseAreas.size() == courseAreas.size();
        setTextOfDeselectButton(courseAreaDeselectButton, allCourseAreasSelected);
        
        List<String> selectedRegattas = new ArrayList<String>();
        for (Entry<String, CheckBox> entry : regattaCheckBoxMap.entrySet()) {
            if (entry.getValue().getValue()) {
                selectedRegattas.add(entry.getKey());
            }
        }
        boolean allRegattasSelected = selectedRegattas.size() == raceGroups.size();
        setTextOfDeselectButton(regattaDeselectButton, allRegattasSelected);
        
        boolean isShowOnlyRacesOfSameDay = showOnlyRacesOfSameDayCheckBox.getValue();
        boolean isShowOnlyCurrentlyRunningRaces = showOnlyCurrentlyRunningRacesCheckBox.getValue();
        return new RegattaRaceStatesSettings(this.courseAreas, selectedCourseAreas, this.raceGroups, selectedRegattas,
                isShowOnlyRacesOfSameDay, isShowOnlyCurrentlyRunningRaces);
    }

    @Override
    public Validator<RegattaRaceStatesSettings> getValidator() {
        return new Validator<RegattaRaceStatesSettings>() {
            @Override
            public String getErrorMessage(RegattaRaceStatesSettings settings) {
                return null;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return null;
    }
}
