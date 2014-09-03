package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class RegattasAndRacesDialog extends DataEntryDialog<RegattaDTO>{
    
    
  protected StringMessages stringMessages;
  protected TrackedRacesListComposite trackedRacesListComposite;
//  protected RegattaDTO regatta;

//  protected ListBox regattasListBox;
//  protected ListBox racesListBox;
//  protected ListBox sailingEventsListBox;
//  protected CheckBox useStartTimeInferenceCheckBox;

  protected List<EventDTO> existingEvents;

  protected abstract void setupAdditionalWidgetsOnPanel(VerticalPanel panel);

  public RegattasAndRacesDialog(RegattaDTO regatta, List<EventDTO> existingEvents, String title,
          String okButton, StringMessages stringMessages, Validator<RegattaDTO> validator,
          DialogCallback<RegattaDTO> callback) {
      super(title, null, okButton, stringMessages.cancel(), validator, callback);
      this.stringMessages = stringMessages;
//      this.regatta = regatta;
      this.existingEvents = existingEvents;
//      regattasListBox = createListBox(true);
//      regattasListBox.ensureDebugId("RegattasListBox");
//      for (ScoringSchemeType scoringSchemeType : ScoringSchemeType.values()) {
//          regattasListBox.addItem(ScoringSchemeTypeFormatter.format(scoringSchemeType, stringMessages),
//                  String.valueOf(scoringSchemeType.ordinal()));
//          if (scoringSchemeType == regatta.scoringScheme) {
//              regattasListBox.setSelectedIndex(regattasListBox.getItemCount() - 1);
//          }
//      }
//      sailingEventsListBox = createListBox(true);
//      sailingEventsListBox.ensureDebugId("EventListBox");
////      useStartTimeInferenceCheckBox = createCheckbox(stringMessages.useStartTimeInference());
////      useStartTimeInferenceCheckBox.ensureDebugId("UseStartTimeInferenceCheckBox");
////      useStartTimeInferenceCheckBox.setValue(regatta.useStartTimeInference);
//      racesListBox = createListBox(true);
//      racesListBox.ensureDebugId("CourseAreaListBox");
//      racesListBox.setEnabled(true);
//      setupEventAndCourseAreaListBoxes(stringMessages);
  }
//
//  @Override
//  protected Widget getAdditionalWidget() {
//      final VerticalPanel panel = new VerticalPanel();
//      Widget additionalWidget = super.getAdditionalWidget();
//      if (additionalWidget != null) {
//          panel.add(additionalWidget);
//      }
//      Grid formGrid = new Grid(3, 2);
//      panel.add(formGrid);
//
//      formGrid.setWidget(0, 0, new Label(stringMessages.regatta() + ":"));
//      formGrid.setWidget(0, 1, regattasListBox);
//      formGrid.setWidget(1, 0, new Label(stringMessages.event() + ":"));
//      formGrid.setWidget(1, 1, sailingEventsListBox);
//      formGrid.setWidget(2, 0, new Label(stringMessages.race() + ":"));
//      formGrid.setWidget(2, 1, racesListBox);
////      formGrid.setWidget(5, 0, new Label(stringMessages.useStartTimeInference() + ":"));
////      formGrid.setWidget(5, 1, useStartTimeInferenceCheckBox);
//      setupAdditionalWidgetsOnPanel(panel);
//      return panel;
//  }
  
//  @Override
//  protected RegattaDTO getResult() {
//      regatta.setName(nameEntryField.getText());
//      regatta.boatClass = new BoatClassDTO(boatClassEntryField.getText(), 0.0);
//      regatta .scoringScheme = getSelectedScoringSchemeType();
//      regatta.useStartTimeInference = useStartTimeInferenceCheckBox.getValue();
//      setCourseAreaInRegatta(regatta);
//      return regatta;
//  }

//  private void setCourseAreaInRegatta(RegattaDTO regatta) {
//      CourseAreaDTO courseArea = getSelectedCourseArea();
//      if (courseArea == null) {
//          regatta.defaultCourseAreaUuid = null;
//      } else {
//          regatta.defaultCourseAreaUuid = courseArea.id;
//          regatta.defaultCourseAreaName = courseArea.getName();
//      }
//  }


//  private void setupEventAndCourseAreaListBoxes(StringMessages stringMessages) {
//      sailingEventsListBox.addItem(stringMessages.selectSailingEvent());
//      for (EventDTO event : existingEvents) {
//          sailingEventsListBox.addItem(event.getName());
//      }
//      sailingEventsListBox.addChangeHandler(new ChangeHandler() {
//          @Override
//          public void onChange(ChangeEvent event) {
//              onEventSelectionChanged();
//          }
//      });
//      regattasListBox.addChangeHandler(new ChangeHandler() {
//          @Override
//          public void onChange(ChangeEvent event) {
//              onRegattaSelectionChanged();
//          }
//      });
//  }
//
//  private void onEventSelectionChanged() {
//      EventDTO selectedEvent = getSelectedEvent();
//      regattasListBox.clear();
//      regattasListBox.setEnabled(false);
//      if (selectedEvent != null) {
//          fillRegattaListBox(selectedEvent);
//      }
//  }
//  
//  private void onRegattaSelectionChanged() {
//      RegattaDTO selectedRegatta = getSelectedRegatta();
//      racesListBox.clear();
//      racesListBox.setEnabled(false);
//      if (selectedRegatta != null) {
//          fillRaceListBox(selectedRegatta);
//      }
//  }
//
//  
//  private void fillRegattaListBox(EventDTO selectedEvent) {
//      regattasListBox.addItem(stringMessages.selectCourseArea());
//      for (RegattaDTO regattaDTO : selectedEvent.regattas) {
//          regattasListBox.addItem(regattaDTO.getName());
////          if (courseArea.id.equals(regatta.defaultCourseAreaUuid)) {
//              regattasListBox.setSelectedIndex(regattasListBox.getItemCount() - 1);
////          }
//      }
//      regattasListBox.setEnabled(true);
//  }
//  
//  private void fillRaceListBox(RegattaDTO selectedRegatta) {
//      racesListBox.addItem(stringMessages.selectRaces());
//      for (RaceDTO raceDTO : selectedRegatta.races) {
//          racesListBox.addItem(raceDTO.getName());
//              racesListBox.setSelectedIndex(racesListBox.getItemCount() - 1);
//      }
//      regattasListBox.setEnabled(true);
//  }
//
//  public EventDTO getSelectedEvent() {
//      EventDTO result = null;
//      int selIndex = sailingEventsListBox.getSelectedIndex();
//      if (selIndex > 0) { // the zero index represents the 'no selection' text
//          String itemText = sailingEventsListBox.getItemText(selIndex);
//          for (EventDTO eventDTO : existingEvents) {
//              if (eventDTO.getName().equals(itemText)) {
//                  result = eventDTO;
//                  break;
//              }
//          }
//      }
//      return result;
//  }
//  
//  public RegattaDTO getSelectedRegatta() {
//      RegattaDTO result = null;
//      EventDTO event = getSelectedEvent();
//      int selIndex = regattasListBox.getSelectedIndex();
//      if (selIndex > 0 && event != null) { // the zero index represents the 'no selection' text
//          String itemText = regattasListBox.getItemText(selIndex);
//          for (RegattaDTO regattaDTO : event.regattas) {
//              if (regattaDTO.getName().equals(itemText)) {
//                  result = regattaDTO;
//                  break;
//              }
//          }
//      }
//      return result;
//  }
//  
//  public RaceDTO getSelectedRace() {
//      RaceDTO result = null;
//      RegattaDTO regatta = getSelectedRegatta();
//      int selIndex = racesListBox.getSelectedIndex();
//      if (selIndex > 0 && regatta != null) { // the zero index represents the 'no selection' text
//          String itemText = racesListBox.getItemText(selIndex);
//          for (RaceDTO raceDTO : regatta.races) {
//              if (raceDTO.getName().equals(itemText)) {
//                  result = raceDTO;
//                  break;
//              }
//          }
//      }
//      return result;
//  }
//  
//  private void openCreateRegattaDialog() {
//      final Collection<RegattaDTO> existingRegattas = Collections.unmodifiableCollection(regattaListComposite.getAllRegattas());
//
//      sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
//          @Override
//          public void onFailure(Throwable caught) {
//              openCreateRegattaDialog(existingRegattas, Collections.<EventDTO>emptyList());
//          }
//
//          @Override
//          public void onSuccess(List<EventDTO> result) {
//              openCreateRegattaDialog(existingRegattas, Collections.unmodifiableList(result));
//          }
//      });
//  }
  
  
  
//  public class TrackedRacesListComposite extends AbstractTrackedRacesListComposite {
//      private final Set<TrackedRaceChangedListener> raceIsTrackedRaceChangeListener;
//      private Button btnUntrack;
//      private Button btnRemoveRace;
//      private Button btnSetDelayToLive;
//      private Button btnExport;
//      private ExportPopup exportPopup;
//
//      public TrackedRacesListComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
//              final RegattaRefresher regattaRefresher, RaceSelectionProvider raceSelectionProvider,
//              final StringMessages stringMessages, boolean hasMultiSelection) {
//          super(sailingService, errorReporter, regattaRefresher, raceSelectionProvider, stringMessages, hasMultiSelection);
//          this.raceIsTrackedRaceChangeListener = new HashSet<TrackedRaceChangedListener>();
//      }
//
//      private void showSetDelayToLiveDialog() {
//          TrackedRacesSettings settings = new TrackedRacesSettings();
//          settings.setDelayToLiveInSeconds(DEFAULT_LIVE_DELAY_IN_MILLISECONDS);
//          
//          SettingsDialog<TrackedRacesSettings> settingsDialog = new SettingsDialog<TrackedRacesSettings>(this, stringMessages);
//          settingsDialog.show();
//      }
//      
//      public void addTrackedRaceChangeListener(TrackedRaceChangedListener listener) {
//          this.raceIsTrackedRaceChangeListener.add(listener);
//      }
//
//      private void stopTrackingRaces(final Iterable<RaceDTO> races) {
//          final List<RegattaAndRaceIdentifier> racesToStopTracking = new ArrayList<RegattaAndRaceIdentifier>();
//          for (RaceDTO race : races) {
//              if (race.isTracked) {
//                  racesToStopTracking.add(race.getRaceIdentifier());
//              }
//          }
//          sailingService.stopTrackingRaces(racesToStopTracking, new MarkedAsyncCallback<Void>(
//                  new AsyncCallback<Void>() {
//                      @Override
//                      public void onFailure(Throwable caught) {
//                          errorReporter.reportError("Exception trying to stop tracking races " + races + ": " + caught.getMessage());
//                      }
//          
//                      @Override
//                      public void onSuccess(Void result) {
//                          regattaRefresher.fillRegattas();
//                          for (TrackedRaceChangedListener listener : raceIsTrackedRaceChangeListener) {
//                              listener.changeTrackingRace(racesToStopTracking, false);
//                          }
//                      }
//                  }));
//      }
//
//      private void removeAndUntrackRaces(final Iterable<RaceDTO> races) {
//          final List<RegattaNameAndRaceName> regattaNamesAndRaceNames = new ArrayList<RegattaNameAndRaceName>();
//          for (RaceDTO race : races) {
//              regattaNamesAndRaceNames.add((RegattaNameAndRaceName) race.getRaceIdentifier());
//          }
//          sailingService.removeAndUntrackRaces(regattaNamesAndRaceNames, new MarkedAsyncCallback<Void>(
//                  new AsyncCallback<Void>() {
//                      @Override
//                      public void onFailure(Throwable caught) {
//                          errorReporter.reportError("Exception trying to remove races " + regattaNamesAndRaceNames +
//                                  ": " + caught.getMessage());
//                      }
//
//                      @Override
//                      public void onSuccess(Void result) {
//                          regattaRefresher.fillRegattas();
//                          for (TrackedRaceChangedListener listener : raceIsTrackedRaceChangeListener) {
//                              listener.changeTrackingRace(regattaNamesAndRaceNames, false);
//                          }
//                      }
//                  }));
//      }
//
//      @Override
//      protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel) {
//          btnRemoveRace = new Button(stringMessages.remove());
//          btnRemoveRace.ensureDebugId("RemoveRaceButton");
//          btnRemoveRace.addClickHandler(new ClickHandler() {
//              @Override
//              public void onClick(ClickEvent event) {
//                  removeAndUntrackRaces(getSelectedRaces());
//              }
//          });
//          btnRemoveRace.setEnabled(false);
//          trackedRacesButtonPanel.add(btnRemoveRace);
//          
//          btnUntrack = new Button(stringMessages.stopTracking());
//          btnUntrack.ensureDebugId("StopTrackingButton");
//          btnUntrack.addClickHandler(new ClickHandler() {
//              @Override
//              public void onClick(ClickEvent click) {
//                  stopTrackingRaces(getSelectedRaces());
//              }
//          });
//          btnUntrack.setEnabled(false);
//          trackedRacesButtonPanel.add(btnUntrack);
//          
//          btnSetDelayToLive = new Button(stringMessages.setDelayToLive() + "...");
//          btnSetDelayToLive.ensureDebugId("SetDelayToLiveButton");
//          btnSetDelayToLive.addClickHandler(new ClickHandler() {
//              @Override
//              public void onClick(ClickEvent event) {
//                  showSetDelayToLiveDialog();
//              }
//          });
//          trackedRacesButtonPanel.add(btnSetDelayToLive);
//
//          exportPopup = new ExportPopup(stringMessages);
//          btnExport = new Button(stringMessages.export());
//          btnExport.ensureDebugId("ExportButton");
//          btnExport.addClickHandler(new ClickHandler() {
//              @Override
//              public void onClick(ClickEvent event) {
//                  exportPopup.center(getSelectedRaces());
//              }
//          });
//          btnExport.setEnabled(false);
//          trackedRacesButtonPanel.add(btnExport);
//      }
//
//      @Override
//      protected void makeControlsReactToSelectionChange(List<RaceDTO> selectedRaces) {
//          if (selectedRaces.isEmpty()) {
//              btnRemoveRace.setEnabled(false);
//              btnUntrack.setEnabled(false);
//              btnExport.setEnabled(false);
//          } else {
//              btnRemoveRace.setEnabled(true);
//              btnUntrack.setEnabled(true);
//              btnExport.setEnabled(true);
//          }
//      }
//
//      @Override
//      protected void makeControlsReactToFillRegattas(List<RegattaDTO> regattas) {
//          if (regattas.isEmpty()) {
//              btnUntrack.setVisible(false);
//              btnRemoveRace.setVisible(false);
//              btnSetDelayToLive.setVisible(false);
//              btnExport.setVisible(false);
//          } else {
//              btnUntrack.setVisible(true);
//              btnUntrack.setEnabled(false);
//              btnRemoveRace.setVisible(true);
//              btnRemoveRace.setEnabled(false);
//              btnSetDelayToLive.setVisible(true);
//              btnExport.setVisible(true);
//          }
//      }
//
//      @Override
//      public String getDependentCssClassName() {
//          return "trackedRacesListComposite";
//      }
//
//      
//  }
  

}

