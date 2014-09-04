package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattasAndRacesDialog extends DataEntryDialog<Set<RegattaAndRaceIdentifier>> implements RegattasDisplayer{

    protected StringMessages stringMessages;
    protected final TrackedRacesListComposite trackedRacesListComposite;
    // protected RegattaDTO regatta;

    // protected ListBox regattasListBox;
    // protected ListBox racesListBox;
    // protected ListBox sailingEventsListBox;
    // protected CheckBox useStartTimeInferenceCheckBox;

    protected List<EventDTO> existingEvents;

//    protected abstract void setupAdditionalWidgetsOnPanel(VerticalPanel panel);

    // public RegattasAndRacesDialog(RegattaDTO regatta, List<EventDTO> existingEvents, String title,
    // String okButton, StringMessages stringMessages, Validator<RegattaDTO> validator,
    // DialogCallback<RegattaDTO> callback) {
    public RegattasAndRacesDialog(SailingServiceAsync sailingService, 
            ErrorReporter errorReporter, RegattaRefresher regattaRefresher, 
            StringMessages stringMessages, Validator<Set<RegattaAndRaceIdentifier>> validator, DialogCallback<Set<RegattaAndRaceIdentifier>> callback) {
        super(stringMessages.addRegatta(), null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        // this.regatta = regatta;
        // this.existingEvents = existingEvents;
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                new RaceSelectionModel(), stringMessages, /* multiselection */true);
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
        regattaRefresher.fillRegattas();

        // regattasListBox = createListBox(true);
        // regattasListBox.ensureDebugId("RegattasListBox");
        // for (ScoringSchemeType scoringSchemeType : ScoringSchemeType.values()) {
        // regattasListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringMessages),
        // String.valueOf(scoringSchemeType.ordinal()));
        // if (scoringSchemeType == regatta.scoringScheme) {
        // regattasListBox.setSelectedIndex(regattasListBox.getItemCount() - 1);
        // }
        // }
        // sailingEventsListBox = createListBox(true);
        // sailingEventsListBox.ensureDebugId("EventListBox");
        // // useStartTimeInferenceCheckBox = createCheckbox(stringMessages.useStartTimeInference());
        // // useStartTimeInferenceCheckBox.ensureDebugId("UseStartTimeInferenceCheckBox");
        // // useStartTimeInferenceCheckBox.setValue(regatta.useStartTimeInference);
        // racesListBox = createListBox(true);
        // racesListBox.ensureDebugId("CourseAreaListBox");
        // racesListBox.setEnabled(true);
        // setupEventAndCourseAreaListBoxes(stringMessages);
    }

    
     @Override
     protected Widget getAdditionalWidget() {
     final VerticalPanel panel = new VerticalPanel();
     Widget additionalWidget = super.getAdditionalWidget();
     if (additionalWidget != null) {
     panel.add(additionalWidget);
     }
     Grid formGrid = new Grid(1,1);
     panel.add(formGrid);
     
     formGrid.setWidget(0, 0, trackedRacesListComposite);
    
//     formGrid.setWidget(0, 0, new Label(stringMessages.regatta() + ":"));
//     formGrid.setWidget(0, 1, regattasListBox);
//     formGrid.setWidget(1, 0, new Label(stringMessages.event() + ":"));
//     formGrid.setWidget(1, 1, sailingEventsListBox);
//     formGrid.setWidget(2, 0, new Label(stringMessages.race() + ":"));
//     formGrid.setWidget(2, 1, racesListBox);
     // formGrid.setWidget(5, 0, new Label(stringMessages.useStartTimeInference() + ":"));
     // formGrid.setWidget(5, 1, useStartTimeInferenceCheckBox);
//     setupAdditionalWidgetsOnPanel(panel);
     return panel;
     }

    @Override
    protected Set<RegattaAndRaceIdentifier> getResult() {
//        regatta.setName(nameEntryField.getText());
//        regatta.boatClass = new BoatClassDTO(boatClassEntryField.getText(), 0.0);
//        regatta.scoringScheme = getSelectedScoringSchemeType();
//        regatta.useStartTimeInference = useStartTimeInferenceCheckBox.getValue();
//        setCourseAreaInRegatta(regatta);
//        return regatta;
        return getSelectedRegattasAndRaces();
    }


    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        // TODO Auto-generated method stub
        this.trackedRacesListComposite.fillRegattas(result);
    }
    
    public Set<RegattaAndRaceIdentifier> getSelectedRegattasAndRaces(){
        List<RaceDTO> races = trackedRacesListComposite.getSelectedRaces();
        Set<RegattaAndRaceIdentifier> regattasAndRaces = new HashSet<RegattaAndRaceIdentifier>();
        for(RaceDTO race: races){
            RegattaAndRaceIdentifier regattaNameAndRaceName = new RegattaNameAndRaceName(race.getRegattaName(), race.getName());
            regattasAndRaces.add(regattaNameAndRaceName);
        }
        return regattasAndRaces;
    }

    // private void setCourseAreaInRegatta(RegattaDTO regatta) {
    // CourseAreaDTO courseArea = getSelectedCourseArea();
    // if (courseArea == null) {
    // regatta.defaultCourseAreaUuid = null;
    // } else {
    // regatta.defaultCourseAreaUuid = courseArea.id;
    // regatta.defaultCourseAreaName = courseArea.getName();
    // }
    // }

    // private void setupEventAndCourseAreaListBoxes(StringMessages stringMessages) {
    // sailingEventsListBox.addItem(stringMessages.selectSailingEvent());
    // for (EventDTO event : existingEvents) {
    // sailingEventsListBox.addItem(event.getName());
    // }
    // sailingEventsListBox.addChangeHandler(new ChangeHandler() {
    // @Override
    // public void onChange(ChangeEvent event) {
    // onEventSelectionChanged();
    // }
    // });
    // regattasListBox.addChangeHandler(new ChangeHandler() {
    // @Override
    // public void onChange(ChangeEvent event) {
    // onRegattaSelectionChanged();
    // }
    // });
    // }
    //
    // private void onEventSelectionChanged() {
    // EventDTO selectedEvent = getSelectedEvent();
    // regattasListBox.clear();
    // regattasListBox.setEnabled(false);
    // if (selectedEvent != null) {
    // fillRegattaListBox(selectedEvent);
    // }
    // }
    //
    // private void onRegattaSelectionChanged() {
    // RegattaDTO selectedRegatta = getSelectedRegatta();
    // racesListBox.clear();
    // racesListBox.setEnabled(false);
    // if (selectedRegatta != null) {
    // fillRaceListBox(selectedRegatta);
    // }
    // }
    //
    //
    // private void fillRegattaListBox(EventDTO selectedEvent) {
    // regattasListBox.addItem(stringMessages.selectCourseArea());
    // for (RegattaDTO regattaDTO : selectedEvent.regattas) {
    // regattasListBox.addItem(regattaDTO.getName());
    // // if (courseArea.id.equals(regatta.defaultCourseAreaUuid)) {
    // regattasListBox.setSelectedIndex(regattasListBox.getItemCount() - 1);
    // // }
    // }
    // regattasListBox.setEnabled(true);
    // }
    //
    // private void fillRaceListBox(RegattaDTO selectedRegatta) {
    // racesListBox.addItem(stringMessages.selectRaces());
    // for (RaceDTO raceDTO : selectedRegatta.races) {
    // racesListBox.addItem(raceDTO.getName());
    // racesListBox.setSelectedIndex(racesListBox.getItemCount() - 1);
    // }
    // regattasListBox.setEnabled(true);
    // }
    //
    // public EventDTO getSelectedEvent() {
    // EventDTO result = null;
    // int selIndex = sailingEventsListBox.getSelectedIndex();
    // if (selIndex > 0) { // the zero index represents the 'no selection' text
    // String itemText = sailingEventsListBox.getItemText(selIndex);
    // for (EventDTO eventDTO : existingEvents) {
    // if (eventDTO.getName().equals(itemText)) {
    // result = eventDTO;
    // break;
    // }
    // }
    // }
    // return result;
    // }
    //
    // public RegattaDTO getSelectedRegatta() {
    // RegattaDTO result = null;
    // EventDTO event = getSelectedEvent();
    // int selIndex = regattasListBox.getSelectedIndex();
    // if (selIndex > 0 && event != null) { // the zero index represents the 'no selection' text
    // String itemText = regattasListBox.getItemText(selIndex);
    // for (RegattaDTO regattaDTO : event.regattas) {
    // if (regattaDTO.getName().equals(itemText)) {
    // result = regattaDTO;
    // break;
    // }
    // }
    // }
    // return result;
    // }
    //
    // public RaceDTO getSelectedRace() {
    // RaceDTO result = null;
    // RegattaDTO regatta = getSelectedRegatta();
    // int selIndex = racesListBox.getSelectedIndex();
    // if (selIndex > 0 && regatta != null) { // the zero index represents the 'no selection' text
    // String itemText = racesListBox.getItemText(selIndex);
    // for (RaceDTO raceDTO : regatta.races) {
    // if (raceDTO.getName().equals(itemText)) {
    // result = raceDTO;
    // break;
    // }
    // }
    // }
    // return result;
    // }
    //
    // private void openCreateRegattaDialog() {
    // final Collection<RegattaDTO> existingRegattas =
    // Collections.unmodifiableCollection(regattaListComposite.getAllRegattas());
    //
    // sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
    // @Override
    // public void onFailure(Throwable caught) {
    // openCreateRegattaDialog(existingRegattas, Collections.<EventDTO>emptyList());
    // }
    //
    // @Override
    // public void onSuccess(List<EventDTO> result) {
    // openCreateRegattaDialog(existingRegattas, Collections.unmodifiableList(result));
    // }
    // });
    // }

}
