package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;
import java.util.UUID;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;

/**
 * Uses a {@link RegattaDTO} to initialize the view. {@link #getRegattaDTO()} can be used by implementations of
 * {@link #getResult()} to produce a result regatta object and always returns a new {@link RegattaDTO} object.
 * The original {@link RegattaDTO} passed to the constructor is not modified by this dialog.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public abstract class AbstractRegattaWithSeriesAndFleetsDialog<T> extends DataEntryDialogWithBootstrap<T> {

    protected StringMessages stringMessages;
    private final RegattaDTO regatta;
    
    protected final BetterDateTimeBox startDateBox;
    protected final BetterDateTimeBox endDateBox;
    protected final ListBox scoringSchemeListBox;
    protected final ListBox courseAreaListBox;
    protected final ListBox sailingEventsListBox;
    protected final CheckBox useStartTimeInferenceCheckBox;
    private final ListEditorComposite<SeriesDTO> seriesEditor;

    protected final List<EventDTO> existingEvents;

    public AbstractRegattaWithSeriesAndFleetsDialog(RegattaDTO regatta, Iterable<SeriesDTO> series, List<EventDTO> existingEvents,
            String title, String okButton, StringMessages stringMessages, Validator<T> validator, DialogCallback<T> callback) {
        super(title, null, okButton, stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        this.regatta = regatta;
        this.existingEvents = existingEvents;
        startDateBox = createDateTimeBox(regatta.startDate);
        startDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        endDateBox = createDateTimeBox(regatta.endDate);
        endDateBox.setFormat("dd/mm/yyyy hh:ii"); 
        scoringSchemeListBox = createListBox(false);
        scoringSchemeListBox.ensureDebugId("ScoringSchemeListBox");
        for (ScoringSchemeType scoringSchemeType : ScoringSchemeType.values()) {
            scoringSchemeListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringMessages),
                    String.valueOf(scoringSchemeType.ordinal()));
            if (scoringSchemeType == regatta.scoringScheme) {
                scoringSchemeListBox.setSelectedIndex(scoringSchemeListBox.getItemCount() - 1);
            }
        }
        sailingEventsListBox = createListBox(false);
        sailingEventsListBox.ensureDebugId("EventListBox");
        useStartTimeInferenceCheckBox = createCheckbox(stringMessages.useStartTimeInference());
        useStartTimeInferenceCheckBox.ensureDebugId("UseStartTimeInferenceCheckBox");
        useStartTimeInferenceCheckBox.setValue(regatta.useStartTimeInference);
        courseAreaListBox = createListBox(false);
        courseAreaListBox.ensureDebugId("CourseAreaListBox");
        courseAreaListBox.setEnabled(false);
        this.seriesEditor = createSeriesEditor(series);
        setupEventAndCourseAreaListBoxes(stringMessages);
    }

    protected abstract void setupAdditionalWidgetsOnPanel(VerticalPanel panel);

    protected abstract ListEditorComposite<SeriesDTO> createSeriesEditor(Iterable<SeriesDTO> series);
    
    protected ListEditorComposite<SeriesDTO> getSeriesEditor() {
        return seriesEditor;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        Grid formGrid = new Grid(6, 2);
        panel.add(formGrid);
        formGrid.setWidget(0, 0, new Label(stringMessages.startDate() + ":"));
        formGrid.setWidget(0, 1, startDateBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.endDate() + ":"));
        formGrid.setWidget(1, 1, endDateBox);
        formGrid.setWidget(2, 0, new Label(stringMessages.scoringSystem() + ":"));
        formGrid.setWidget(2, 1, scoringSchemeListBox);
        formGrid.setWidget(3, 0, new Label(stringMessages.event() + ":"));
        formGrid.setWidget(3, 1, sailingEventsListBox);
        formGrid.setWidget(4, 0, new Label(stringMessages.courseArea() + ":"));
        formGrid.setWidget(4, 1, courseAreaListBox);
        formGrid.setWidget(5, 0, new Label(stringMessages.useStartTimeInference() + ":"));
        formGrid.setWidget(5, 1, useStartTimeInferenceCheckBox);
        setupAdditionalWidgetsOnPanel(panel);
        return panel;
    }

    protected void setCourseAreaInRegatta(RegattaDTO regatta) {
        CourseAreaDTO courseArea = getSelectedCourseArea();
        if (courseArea == null) {
            regatta.defaultCourseAreaUuid = null;
        } else {
            regatta.defaultCourseAreaUuid = courseArea.id;
            regatta.defaultCourseAreaName = courseArea.getName();
        }
    }

    public ScoringSchemeType getSelectedScoringSchemeType() {
        int index = scoringSchemeListBox.getSelectedIndex();
        if (index >= 0) {
            return ScoringSchemeType.values()[Integer.valueOf(scoringSchemeListBox.getValue(index))];
        }
        return null;
    }

    private boolean isCourseAreaInEvent(EventDTO event, UUID courseAreaId) {
        if (event.venue == null) {
            return false;
        }
        for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
            if (courseArea.id.equals(courseAreaId)) {
                return true;
            }
        }
        return false;
    }
    
    private void setupEventAndCourseAreaListBoxes(StringMessages stringMessages) {
        sailingEventsListBox.addItem(stringMessages.selectSailingEvent());
        for (EventDTO event : existingEvents) {
            sailingEventsListBox.addItem(event.getName());
            if (isCourseAreaInEvent(event, regatta.defaultCourseAreaUuid)) {
                sailingEventsListBox.setSelectedIndex(sailingEventsListBox.getItemCount() - 1);
                fillCourseAreaListBox(event);
            }
        }
        sailingEventsListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onEventSelectionChanged();
            }
        });
    }

    protected void onEventSelectionChanged() {
        setCourseAreaSelection();
    }

    protected void setCourseAreaSelection() {
        EventDTO selectedEvent = getSelectedEvent();
        courseAreaListBox.clear();
        courseAreaListBox.setEnabled(false);
        if (selectedEvent != null) {
            fillCourseAreaListBox(selectedEvent);
        }
    }

    protected void fillCourseAreaListBox(EventDTO selectedEvent) {
        courseAreaListBox.addItem(stringMessages.selectCourseArea());
        for (CourseAreaDTO courseArea : selectedEvent.venue.getCourseAreas()) {
            courseAreaListBox.addItem(courseArea.getName());
            if (courseArea.id.equals(regatta.defaultCourseAreaUuid)) {
                courseAreaListBox.setSelectedIndex(courseAreaListBox.getItemCount() - 1);
            }
        }
        courseAreaListBox.setEnabled(true);
    }

    public EventDTO getSelectedEvent() {
        EventDTO result = null;
        int selIndex = sailingEventsListBox.getSelectedIndex();
        if (selIndex > 0) { // the zero index represents the 'no selection' text
            String itemText = sailingEventsListBox.getItemText(selIndex);
            for (EventDTO eventDTO : existingEvents) {
                if (eventDTO.getName().equals(itemText)) {
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
        if (selIndex > 0 && event != null) { // the zero index represents the 'no selection' text
            String itemText = courseAreaListBox.getItemText(selIndex);
            for (CourseAreaDTO courseAreaDTO : event.venue.getCourseAreas()) {
                if (courseAreaDTO.getName().equals(itemText)) {
                    result = courseAreaDTO;
                    break;
                }
            }
        }
        return result;
    }
    
    public RegattaDTO getRegattaDTO() {
        RegattaDTO result = new RegattaDTO();
        result.startDate = startDateBox.getValue();
        result.endDate = endDateBox.getValue();
        result.scoringScheme = getSelectedScoringSchemeType();
        result.useStartTimeInference = useStartTimeInferenceCheckBox.getValue();
        setCourseAreaInRegatta(result);
        result.series = getSeriesEditor().getValue();
        return result;
    }

}
