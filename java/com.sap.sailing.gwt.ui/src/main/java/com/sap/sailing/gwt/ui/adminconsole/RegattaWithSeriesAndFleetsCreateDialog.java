package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class RegattaWithSeriesAndFleetsCreateDialog extends DataEntryDialog<RegattaDTO> {

    private StringMessages stringConstants;
    private RegattaDTO regatta;

    private TextBox nameEntryField;
    private TextBox boatClassEntryField;
    private ListBox scoringSchemeListBox;
    private ListBox courseAreaListBox;
    private ListBox sailingEventsListBox;

    private List<SeriesDTO> createdSeries;

    private Grid seriesGrid;
    
    private List<EventDTO> existingEvents;

    protected static class RegattaParameterValidator implements Validator<RegattaDTO> {

        private StringMessages stringConstants;
        private ArrayList<RegattaDTO> existingRegattas;

        public RegattaParameterValidator(StringMessages stringConstants, Collection<RegattaDTO> existingRegattas) {
            this.stringConstants = stringConstants;
            this.existingRegattas = new ArrayList<RegattaDTO>(existingRegattas);
        }

        @Override
        public String getErrorMessage(RegattaDTO regattaToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = regattaToValidate.name != null && regattaToValidate.name.length() > 0;
            boolean boatClassNotEmpty = regattaToValidate.boatClass != null
                    && regattaToValidate.boatClass.name.length() > 0;

            boolean unique = true;
            for (RegattaDTO regatta : existingRegattas) {
                if (regatta.name.equals(regattaToValidate.name)) {
                    unique = false;
                    break;
                }
            }

            if (!nameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterAName();
            } else if (!boatClassNotEmpty) {
                errorMessage = stringConstants.pleaseEnterAName();
            } else if (!unique) {
                errorMessage = stringConstants.regattaWithThisNameAlreadyExists();
            }

            if (errorMessage == null) {
                List<SeriesDTO> seriesToValidate = regattaToValidate.series;
                int index = 0;
                boolean seriesNameNotEmpty = true;

                for (SeriesDTO series : seriesToValidate) {
                    seriesNameNotEmpty = series.name != null && series.name.length() > 0;
                    if (!seriesNameNotEmpty) {
                        break;
                    }
                    index++;
                }

                int index2 = 0;
                boolean seriesUnique = true;

                HashSet<String> setToFindDuplicates = new HashSet<String>();
                for (SeriesDTO series : seriesToValidate) {
                    if (!setToFindDuplicates.add(series.name)) {
                        seriesUnique = false;
                        break;
                    }
                    index2++;
                }

                if (!seriesNameNotEmpty) {
                    errorMessage = stringConstants.series() + " " + (index + 1) + ": "
                            + stringConstants.pleaseEnterAName();
                } else if (!seriesUnique) {
                    errorMessage = stringConstants.series() + " " + (index2 + 1) + ": "
                            + stringConstants.seriesWithThisNameAlreadyExists();
                }

            }

            return errorMessage;
        }

    }

    public RegattaWithSeriesAndFleetsCreateDialog(Collection<RegattaDTO> existingRegattas, List<EventDTO> existingEvents,
            StringMessages stringConstants, DialogCallback<RegattaDTO> callback) {
        super(stringConstants.regatta(), null, stringConstants.ok(), stringConstants.cancel(),
                new RegattaParameterValidator(stringConstants, existingRegattas), callback);
        this.stringConstants = stringConstants;
        this.regatta = new RegattaDTO();
        this.existingEvents = existingEvents;

        nameEntryField = createTextBox(null);
        nameEntryField.setVisibleLength(40);
        boatClassEntryField = createTextBox(null);
        boatClassEntryField.setVisibleLength(20);
        scoringSchemeListBox = createListBox(false);
        for (ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
            scoringSchemeListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringConstants));
        }
        
        sailingEventsListBox = createSailingEventListBox();
        this.courseAreaListBox = createListBox(false);
        this.courseAreaListBox.setEnabled(false);

        createdSeries = new ArrayList<SeriesDTO>();
        seriesGrid = new Grid(0, 0);
    }

    @Override
    protected RegattaDTO getResult() {
        regatta.name = nameEntryField.getText();
        regatta.boatClass = new BoatClassDTO(boatClassEntryField.getText(), 0.0);
        regatta.scoringScheme = getSelectedScoringSchemeType();
        regatta.series = new ArrayList<SeriesDTO>();
        regatta.series.addAll(createdSeries);
        setCourseAreaInRegatta(regatta);
        return regatta;
    }
    
    private void setCourseAreaInRegatta(RegattaDTO regatta) {
        CourseAreaDTO courseArea = getSelectedCourseArea();
        if (courseArea == null) {
            regatta.defaultCourseAreaIdAsString = null;
        } else {
            regatta.defaultCourseAreaIdAsString = courseArea.id;
            regatta.defaultCourseAreaName = courseArea.name;
        }
    }
    
    public ScoringSchemeType getSelectedScoringSchemeType() {
        ScoringSchemeType result = null;
        int selIndex = scoringSchemeListBox.getSelectedIndex();
        if(selIndex >= 0) { 
            String itemText = scoringSchemeListBox.getItemText(selIndex);
            for(ScoringSchemeType scoringSchemeType: ScoringSchemeType.values()) {
                if(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringConstants).equals(itemText)) {
                    result = scoringSchemeType;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        Grid formGrid = new Grid(5, 2);
        panel.add(formGrid);
        formGrid.setWidget(0, 0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 0, new Label(stringConstants.boatClass() + ":"));
        formGrid.setWidget(1, 1, boatClassEntryField);
        formGrid.setWidget(2, 0, new Label(stringConstants.scoringSystem() + ":"));
        formGrid.setWidget(2, 1, scoringSchemeListBox);
        formGrid.setWidget(3, 0, new Label(stringConstants.event() + ":"));
        formGrid.setWidget(3, 1, sailingEventsListBox);
        formGrid.setWidget(4, 0, new Label(stringConstants.courseArea() + ":"));
        formGrid.setWidget(4, 1, courseAreaListBox);
        
        panel.add(createHeadlineLabel(stringConstants.series()));
        panel.add(seriesGrid);
        Button addSeriesButton = new Button("Add series");
        addSeriesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RegattaDTO result = getResult();
                SeriesWithFleetsCreateDialog dialog = new SeriesWithFleetsCreateDialog(Collections
                        .unmodifiableCollection(result.series), stringConstants, new DialogCallback<SeriesDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(SeriesDTO newSeries) {
                        createdSeries.add(newSeries);
                        updateSeriesGrid(panel);
                    }
                });
                dialog.show();
            }
        });
        panel.add(addSeriesButton);
        return panel;
    }

    private void updateSeriesGrid(VerticalPanel parentPanel) {
        int widgetIndex = parentPanel.getWidgetIndex(seriesGrid);
        parentPanel.remove(seriesGrid);

        int seriesCount = createdSeries.size();
        seriesGrid = new Grid(seriesCount * 2, 3);
        seriesGrid.setCellSpacing(3);

        for (int i = 0; i < seriesCount; i++) {
            SeriesDTO seriesDTO = createdSeries.get(i);
            Label seriesLabel = new Label((i + 1) + ". " + stringConstants.series() + ":");
            seriesLabel.setWordWrap(false);
            seriesLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            seriesGrid.setWidget(i * 2, 0, seriesLabel);
            seriesGrid.setHTML(i * 2, 1, seriesDTO.name);
            if (seriesDTO.getFleets() != null && seriesDTO.getFleets().size() > 0) {
                seriesGrid.setHTML(i * 2 + 1, 1, seriesDTO.getFleets().size() + " fleets: "
                        + seriesDTO.getFleets().toString());
            } else {
                seriesGrid.setHTML(i * 2 + 1, 1, seriesDTO.getFleets().size() + " No fleets defined.");
            }
        }

        parentPanel.insert(seriesGrid, widgetIndex);
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }
    
    protected ListBox createSailingEventListBox() {
        ListBox eventListBox = createListBox(false);
        eventListBox.addItem("Please select a sailing event...");
        for (EventDTO event: existingEvents) {
                eventListBox.addItem(event.name);
        }
        eventListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onEventSelectionChanged();
            }
        });
        return eventListBox;
    }
    
    protected void onEventSelectionChanged() {
        EventDTO selectedEvent = getSelectedEvent();
        courseAreaListBox.clear();
        courseAreaListBox.addItem("Please select a course area...");
        for (CourseAreaDTO courseArea : selectedEvent.venue.getCourseAreas()) {
            courseAreaListBox.addItem(courseArea.name);
        }
        courseAreaListBox.setEnabled(true);
    }
    
    public EventDTO getSelectedEvent() {
        EventDTO result = null;
        int selIndex = sailingEventsListBox.getSelectedIndex();
        if(selIndex > 0) { // the zero index represents the 'no selection' text
            String itemText = sailingEventsListBox.getItemText(selIndex);
            for(EventDTO eventDTO: existingEvents) {
                if(eventDTO.name.equals(itemText)) {
                    result = eventDTO;
                    break;
                }
            }
        }
        return result;
    }
    
    public CourseAreaDTO getSelectedCourseArea() {
        CourseAreaDTO result = null;
        EventDTO event = getSelectedEvent();
        int selIndex = courseAreaListBox.getSelectedIndex();
        if(selIndex > 0 && event != null) { // the zero index represents the 'no selection' text
            String itemText = courseAreaListBox.getItemText(selIndex);
            for(CourseAreaDTO courseAreaDTO: event.venue.getCourseAreas()) {
                if(courseAreaDTO.name.equals(itemText)) {
                    result = courseAreaDTO;
                    break;
                }
            }
        }
        return result;
    }

}
