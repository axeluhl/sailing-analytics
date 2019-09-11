package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.client.AbstractRegattaPanel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.UserService;

public abstract class AbstractEventManagementPanel extends AbstractRegattaPanel {
    protected final TrackedRacesListComposite trackedRacesListComposite;
    private final List<RegattaDTO> availableRegattas;
    private final ListBox availableRegattasListBox;
    
    public AbstractEventManagementPanel(SailingServiceAsync sailingService, UserService userService,
            RegattaRefresher regattaRefresher, ErrorReporter errorReporter, boolean actionButtonsEnabled,
            StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, stringMessages);
        this.availableRegattas = new ArrayList<RegattaDTO>();
        
        this.availableRegattasListBox = new ListBox();
        this.availableRegattasListBox.ensureDebugId("AvailableRegattasListBox");
        
        // TrackedEventsComposite should exist in every *ManagementPanel. 
        trackedRacesListComposite = new TrackedRacesListComposite(null, null, sailingService, userService,
                errorReporter, regattaRefresher, stringMessages, /* multiselection */ true, actionButtonsEnabled);
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
    }
    
    @Override
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        this.trackedRacesListComposite.fillRegattas(regattas);
        RegattaDTO selectedRegatta = getSelectedRegatta();
        this.availableRegattas.clear();
        this.availableRegattasListBox.clear();
        this.availableRegattasListBox.addItem(this.stringMessages.defaultRegatta());
        List<RegattaDTO> regattasSortedByName = new ArrayList<RegattaDTO>();
        Util.addAll(regattas, regattasSortedByName);
        Collections.sort(regattasSortedByName, new Comparator<RegattaDTO>() {
            @Override
            public int compare(RegattaDTO o1, RegattaDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (RegattaDTO regatta : regattasSortedByName) {
            this.availableRegattas.add(regatta);
            this.availableRegattasListBox.addItem(regatta.getName());
            if(selectedRegatta != null && selectedRegatta.getName().equals(regatta.getName())) {
                this.availableRegattasListBox.setSelectedIndex(this.availableRegattasListBox.getItemCount() - 1);
            }
        }
    }

    protected List<RegattaDTO> getAvailableRegattas() {
        return availableRegattas;
    }

    protected ListBox getAvailableRegattasListBox() {
        return availableRegattasListBox;
    }

    /**
     * @return <code>null</code> in case of the "Default Regatta" choice
     */
    public RegattaDTO getSelectedRegatta() {
        RegattaDTO result = null;
        int selIndex = this.availableRegattasListBox.getSelectedIndex();
        // the zero index represents the 'no selection' text
        if (selIndex > 0) {
            String itemValue = this.availableRegattasListBox.getValue(selIndex);
            for (RegattaDTO regatta : this.availableRegattas) {
                if (regatta.getName().equals(itemValue)) {
                    result = regatta;
                    break;
                }
            }
        }
        return result;
    }

    protected boolean checkBoatClassOK(RegattaDTO selectedRegatta, Iterable<? extends RaceRecordDTO> selectedRaces) {
        final boolean result;
        if (selectedRegatta != null) {
            List<RaceRecordDTO> racesWithNotMatchingBoatClasses = new ArrayList<>();
            // show an error and don't load anything if the boat class of any race doesn't match the boat class of the selected regatta;
            // show the user the names of the offending races
            for (RaceRecordDTO race : selectedRaces) {
                if (!checkBoatClassMatch(race.getBoatClassNames(), selectedRegatta)) {
                    racesWithNotMatchingBoatClasses.add(race);
                }
            }
            if (!racesWithNotMatchingBoatClasses.isEmpty()) {
                StringBuilder builder = new StringBuilder(100 + racesWithNotMatchingBoatClasses.size() * 30);
                builder.append(stringMessages
                        .boatClassDoesNotMatchSelectedRegatta(selectedRegatta.boatClass == null ? ""
                                : selectedRegatta.boatClass.getName()));
                builder.append("\n\n");
                builder.append(stringMessages.races());
                builder.append("\n");
                for (RaceRecordDTO record: racesWithNotMatchingBoatClasses) {
                    builder.append(record.getName());
                    builder.append(" (");
                    builder.append(record.getBoatClassNames());
                    builder.append(")");
                    builder.append("\n");
                }
                Notification.notify(builder.toString(), NotificationType.ERROR);
                result = false;
            } else {
                result = true;
            }
        } else {
            // "Default Regatta" selected; check if for races for which no previous regatta assignment is known there is
            // another regatta with the same boat class into which the user may want to load
            Map<RaceRecordDTO, Set<RegattaDTO>> existingRegattasWithMatchingBoatClassPerRace = new HashMap<>();
            for (RaceRecordDTO race : selectedRaces) {
                if (!race.hasRememberedRegatta()) {
                    Set<RegattaDTO> existingRegattasWithMatchingBoatClass = new LinkedHashSet<>();
                    for (RegattaDTO regatta : getAvailableRegattas()) {
                        if (checkBoatClassMatch(race.getBoatClassNames(), regatta)) {
                            existingRegattasWithMatchingBoatClass.add(regatta);
                        }
                    }
                    if (!existingRegattasWithMatchingBoatClass.isEmpty()) {
                        existingRegattasWithMatchingBoatClassPerRace.put(race, existingRegattasWithMatchingBoatClass);
                    }
                }
            }
            if (!existingRegattasWithMatchingBoatClassPerRace.isEmpty()) {
                StringBuilder builder = new StringBuilder(100 + existingRegattasWithMatchingBoatClassPerRace.size() * 30);
                builder.append(stringMessages.regattaExistForSelectedBoatClass());
                builder.append("\n\n");
                builder.append(stringMessages.races());
                builder.append("\n");
                for (Entry<RaceRecordDTO, Set<RegattaDTO>> e : existingRegattasWithMatchingBoatClassPerRace.entrySet()) {
                    builder.append(e.getKey().getName());
                    builder.append(" (");
                    builder.append(e.getKey().getBoatClassNames());
                    builder.append("): ");
                    builder.append(e.getValue());
                    builder.append("\n");
                }
                result = Window.confirm(builder.toString());
            } else {
                result = true;
            }
        }
        return result;
    }

    private boolean checkBoatClassMatch(Iterable<String> boatClassNames, RegattaDTO selectedRegatta) {
        Set<BoatClassMasterdata> boatClassNamesMappedToMasterData = new HashSet<>();
        for (String boatClassName : boatClassNames) {
            boatClassNamesMappedToMasterData.add(BoatClassMasterdata.resolveBoatClass(boatClassName));
        }
        return boatClassNamesMappedToMasterData.contains(BoatClassMasterdata.resolveBoatClass(selectedRegatta.boatClass.getName()));
    }

}
