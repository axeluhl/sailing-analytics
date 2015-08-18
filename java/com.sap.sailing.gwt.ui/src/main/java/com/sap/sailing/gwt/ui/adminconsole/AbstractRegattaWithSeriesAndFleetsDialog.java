package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.RankingMetricTypeFormatter;
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
    private final ListBox rankingMetricListBox;

    protected final List<EventDTO> existingEvents;

    public AbstractRegattaWithSeriesAndFleetsDialog(RegattaDTO regatta, Iterable<SeriesDTO> series, List<EventDTO> existingEvents,
            String title, String okButton, StringMessages stringMessages, Validator<T> validator, DialogCallback<T> callback) {
        super(title, null, okButton, stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        this.regatta = regatta;
        this.existingEvents = existingEvents;
        rankingMetricListBox = createListBox(/* isMultipleSelect */ false);
        for (RankingMetrics rankingMetricType : RankingMetrics.values()) {
            rankingMetricListBox.addItem(RankingMetricTypeFormatter.format(rankingMetricType, stringMessages), rankingMetricType.name());
            final SelectElement selectElement = rankingMetricListBox.getElement().cast();
            final NodeList<OptionElement> options = selectElement.getOptions();
            options.getItem(options.getLength()-1).setTitle(RankingMetricTypeFormatter.getDescription(rankingMetricType, stringMessages));
        }
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

    /**
     * Subclasses that want to display a list box for the ranking metric can use this method to create a tab
     * panel that can be inserted into the grid panel in the {@link #setupAdditionalWidgetsOnPanel(VerticalPanel, Grid)} method.
     */
    protected void insertRankingMetricTabPanel(Grid formGrid) {
        int gridRow = formGrid.insertRow(formGrid.getRowCount());
        formGrid.setWidget(gridRow, 0, new Label(stringMessages.rankingMetric()));
        formGrid.setWidget(gridRow, 1, rankingMetricListBox);
    }

    protected void setRankingMetrics(RegattaDTO dto) {
        dto.rankingMetricType = RankingMetrics.valueOf(getRankingMetricListBox().getValue(getRankingMetricListBox().getSelectedIndex()));
    }

    protected ListBox getRankingMetricListBox() {
        return rankingMetricListBox;
    }
    
   /**
     * @param panel
     *            the panel holding dialog elements
     * @param formGrid
     *            a grid at the top of the <code>panel</code> in which the default label/checkbox pairs are presented;
     *            implementors may use this to {@link Grid#insertRow(int) insert} more rows which then are formatted
     *            properly together with the label/checkbox pairs provided by this class.
     */
    protected abstract void setupAdditionalWidgetsOnPanel(VerticalPanel panel, Grid formGrid);

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
        setupAdditionalWidgetsOnPanel(panel, formGrid);
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
        final List<EventDTO> sortedEvents = new ArrayList<>();
        sortedEvents.addAll(existingEvents);
        Collections.sort(sortedEvents, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO o1, EventDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (EventDTO event : sortedEvents) {
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
