package com.sap.sailing.gwt.ui.adminconsole;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithDateTimeBox;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.common.client.RandomString;
import com.sap.sailing.gwt.ui.leaderboard.RankingMetricTypeFormatter;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.controls.datetime.DateAndTimeInput;
import com.sap.sse.gwt.client.controls.datetime.DateTimeInput.Accuracy;
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
public abstract class AbstractRegattaWithSeriesAndFleetsDialog<T> extends DataEntryDialogWithDateTimeBox<T> {

    protected final SailingServiceAsync sailingService; 
    protected StringMessages stringMessages;
    private final RegattaDTO regatta;

    protected final DateAndTimeInput startDateBox;
    protected final DateAndTimeInput endDateBox;
    protected final ListBox scoringSchemeListBox;
    protected final ListBox courseAreaListBox;
    protected final ListBox sailingEventsListBox;
    protected final CheckBox useStartTimeInferenceCheckBox;
    protected final CheckBox controlTrackingFromStartAndFinishTimesCheckBox;
    protected final DoubleBox buoyZoneRadiusInHullLengthsDoubleBox;
    protected final ListEditorComposite<SeriesDTO> seriesEditor;
    private final ListBox rankingMetricListBox;
    protected final ListBox competitorRegistrationTypeListBox;
    protected final Button registrationLinkWithQRCodeOpenButton;

    protected final List<EventDTO> existingEvents;
    private EventDTO defaultEvent;
    private RegistrationLinkWithQRCode registrationLinkWithQRCode;

    protected final CaptionPanel secretPanel;

    public AbstractRegattaWithSeriesAndFleetsDialog(final SailingServiceAsync sailingService, RegattaDTO regatta,
            Iterable<SeriesDTO> series, List<EventDTO> existingEvents, EventDTO correspondingEvent, String title,
            String okButton, StringMessages stringMessages, Validator<T> validator, DialogCallback<T> callback) {
        super(title, null, okButton, stringMessages.cancel(), validator, callback);
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.regatta = regatta;
        this.defaultEvent = correspondingEvent;
        this.existingEvents = existingEvents;
        rankingMetricListBox = createListBox(/* isMultipleSelect */ false);
        for (RankingMetrics rankingMetricType : RankingMetrics.values()) {
            rankingMetricListBox.addItem(RankingMetricTypeFormatter.format(rankingMetricType, stringMessages), rankingMetricType.name());
            final SelectElement selectElement = rankingMetricListBox.getElement().cast();
            final NodeList<OptionElement> options = selectElement.getOptions();
            options.getItem(options.getLength()-1).setTitle(RankingMetricTypeFormatter.getDescription(rankingMetricType, stringMessages));
        }
        startDateBox = createDateTimeBox(regatta.startDate, Accuracy.MINUTES);
        startDateBox.ensureDebugId("StartDateTimeBox");
        endDateBox = createDateTimeBox(regatta.endDate, Accuracy.MINUTES);
        endDateBox.ensureDebugId("EndDateTimeBox");
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
        controlTrackingFromStartAndFinishTimesCheckBox = createCheckbox(stringMessages.controlTrackingFromStartAndFinishTimes());
        controlTrackingFromStartAndFinishTimesCheckBox.ensureDebugId("ControlTrackingFromStartAndFinishTimesCheckBox");
        controlTrackingFromStartAndFinishTimesCheckBox.setValue(regatta.controlTrackingFromStartAndFinishTimes);

        buoyZoneRadiusInHullLengthsDoubleBox = createDoubleBox(regatta.buoyZoneRadiusInHullLengths, 10);
        buoyZoneRadiusInHullLengthsDoubleBox.ensureDebugId("BuoyZoneRadiusInHullLengthsDoubleBox");

        courseAreaListBox = createListBox(false);
        courseAreaListBox.ensureDebugId("CourseAreaListBox");
        courseAreaListBox.setEnabled(false);
        this.seriesEditor = createSeriesEditor(series);
        setupEventAndCourseAreaListBoxes(stringMessages);

        competitorRegistrationTypeListBox = createListBox(false);
        competitorRegistrationTypeListBox.ensureDebugId("CompetitorRegistrationTypeListBox");
        EnumSet.allOf(CompetitorRegistrationType.class).forEach(t->competitorRegistrationTypeListBox.addItem(t.getLabel(stringMessages), t.name()));
        competitorRegistrationTypeListBox.setSelectedIndex(regatta.competitorRegistrationType.ordinal());

        registrationLinkWithQRCode = new RegistrationLinkWithQRCode();
        if (regatta.registrationLinkSecret == null) {
            registrationLinkWithQRCode.setSecret(RandomString.createRandomSecret(20));
        } else {
            registrationLinkWithQRCode.setSecret(regatta.registrationLinkSecret);
        }
        registrationLinkWithQRCodeOpenButton = new Button(stringMessages.registrationLinkConfig(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RegistrationLinkWithQRCodeDialog dialog = new RegistrationLinkWithQRCodeDialog(sailingService, stringMessages, regatta.getName(),
                        registrationLinkWithQRCode, /* editMode */ true, new DialogCallback<RegistrationLinkWithQRCode>() {
                            @Override
                            public void ok(RegistrationLinkWithQRCode result) {
                                registrationLinkWithQRCode = result;
                            }

                            @Override
                            public void cancel() {
                            }
                        });
                dialog.ensureDebugId("RegistrationLinkWithQRCodeDialog");
                dialog.show();

            }
        });
        registrationLinkWithQRCodeOpenButton.ensureDebugId("RegistrationLinkWithQRCodeOpenButton");

        competitorRegistrationTypeListBox.addChangeHandler(e -> {
            updateConfigureButton();
        });
        
        updateConfigureButton();

        // secret panel
        secretPanel = new CaptionPanel(stringMessages.registrationLinkSecret());
        createSecretPanel(stringMessages, secretPanel);
    }

    private void createSecretPanel(final StringMessages stringMessages, final CaptionPanel secretPanel) {
        final VerticalPanel secretPanelContent = new VerticalPanel();
        secretPanel.add(secretPanelContent);

        // explain Label with description of secret
        final Label secretExplainLabel = new Label(stringMessages.registrationLinkSecretExplain());
        secretPanelContent.add(secretExplainLabel);
        secretExplainLabel.setWordWrap(true);

        // Label
        Label secretLabel = new Label(stringMessages.registrationLinkSecret() + ":");

        // Textbox
        final TextBox secretTextBox = createTextBox(registrationLinkWithQRCode.getSecret(), 30);
        secretTextBox.ensureDebugId("SecretTextBox");

        // Generate-Button
        final Button generateSecretButton = new Button(stringMessages.registrationLinkSecretGenerate(),
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        final String randomString = RandomString.createRandomSecret(20);
                        registrationLinkWithQRCode.setSecret(randomString);
                        secretTextBox.setText(randomString);
                    }
                });
        generateSecretButton.ensureDebugId("GenerateSecretButton");

        // add all to grid
        final Grid secretPanelFormGrid = new Grid(1, 3);
        secretPanelFormGrid.setWidget(0, 0, secretLabel);
        secretPanelFormGrid.setWidget(0, 1, secretTextBox);
        secretPanelFormGrid.setWidget(0, 2, generateSecretButton);

        secretPanelContent.add(secretPanelFormGrid);
    }

    private void updateConfigureButton() {
        // show button only if selected CompetitorRegistrationType is Open_Moderated
        final boolean isOpenModerated = CompetitorRegistrationType.OPEN_MODERATED.name()
                .equals(competitorRegistrationTypeListBox.getSelectedValue())
                || CompetitorRegistrationType.OPEN_UNMODERATED.name()
                .equals(competitorRegistrationTypeListBox.getSelectedValue());
        registrationLinkWithQRCodeOpenButton.setVisible(isOpenModerated);
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
        Grid formGrid = new Grid(11, 2);
        panel.add(formGrid);

        formGrid.setWidget(0, 0, new Label(stringMessages.timeZone() + ":"));
        formGrid.setWidget(0, 1, new Label(DateAndTimeFormatterUtil.getClientTimeZoneAsGMTString()));
        formGrid.setWidget(1, 0, new Label(stringMessages.startDate() + ":"));
        formGrid.setWidget(1, 1, startDateBox);
        formGrid.setWidget(2, 0, new Label(stringMessages.endDate() + ":"));
        formGrid.setWidget(2, 1, endDateBox);
        formGrid.setWidget(3, 0, new Label(stringMessages.scoringSystem() + ":"));
        formGrid.setWidget(3, 1, scoringSchemeListBox);
        formGrid.setWidget(4, 0, new Label(stringMessages.event() + ":"));
        formGrid.setWidget(4, 1, sailingEventsListBox);
        formGrid.setWidget(5, 0, new Label(stringMessages.courseArea() + ":"));
        formGrid.setWidget(5, 1, courseAreaListBox);
        formGrid.setWidget(6, 0, new Label(stringMessages.useStartTimeInference() + ":"));
        formGrid.setWidget(6, 1, useStartTimeInferenceCheckBox);
        formGrid.setWidget(7, 0, new Label(stringMessages.controlTrackingFromStartAndFinishTimes() + ":"));
        formGrid.setWidget(7, 1, controlTrackingFromStartAndFinishTimesCheckBox);
        formGrid.setWidget(8, 0, new Label(stringMessages.buoyZoneRadiusInHullLengths() + ":"));
        formGrid.setWidget(8, 1, buoyZoneRadiusInHullLengthsDoubleBox);
        formGrid.setWidget(9, 0, new Label(stringMessages.competitorRegistrationType() + ":"));
        formGrid.setWidget(9, 1, competitorRegistrationTypeListBox);
        formGrid.setWidget(10, 0, new Label(stringMessages.registrationLink() + ":"));
        formGrid.setWidget(10, 1, registrationLinkWithQRCodeOpenButton);

        panel.add(secretPanel);
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
        for (EventDTO event : Util.sortNamedCollection(existingEvents)) {
            sailingEventsListBox.addItem(event.getName());
            if (defaultEvent != null) {
                if (defaultEvent.getName().equals(event.getName())) {
                    sailingEventsListBox.setSelectedIndex(sailingEventsListBox.getItemCount() - 1);
                    fillCourseAreaListBox(event);
                    //select default course area, 2 elements as first is please select course area string
                    if (courseAreaListBox.getItemCount() == 2){
                        courseAreaListBox.setSelectedIndex(1);
                    }
                }
            } else { 
                if (isCourseAreaInEvent(event, regatta.defaultCourseAreaUuid)) {
                    sailingEventsListBox.setSelectedIndex(sailingEventsListBox.getItemCount() - 1);
                    fillCourseAreaListBox(event);
                }
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
            courseAreaListBox.addItem(courseArea.getName(), courseArea.id.toString());
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
            String itemValue = sailingEventsListBox.getValue(selIndex);
            for (EventDTO eventDTO : existingEvents) {
                if (eventDTO.getName().equals(itemValue)) {
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
            String selectedCourseAreaIdAsString = courseAreaListBox.getValue(selIndex);
            for (CourseAreaDTO courseAreaDTO : event.venue.getCourseAreas()) {
                if (courseAreaDTO.id.toString().equals(selectedCourseAreaIdAsString)) {
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
        result.controlTrackingFromStartAndFinishTimes = controlTrackingFromStartAndFinishTimesCheckBox.getValue();
        result.buoyZoneRadiusInHullLengths = buoyZoneRadiusInHullLengthsDoubleBox.getValue();
        setCourseAreaInRegatta(result);
        result.series = getSeriesEditor().getValue();
        result.competitorRegistrationType = CompetitorRegistrationType.valueOf(competitorRegistrationTypeListBox.getSelectedValue());
        if (result.competitorRegistrationType.isOpen() && registrationLinkWithQRCode != null) {
            result.registrationLinkSecret = registrationLinkWithQRCode.getSecret();
        } else {
            result.registrationLinkSecret = null;
        }
        return result;
    }

}
