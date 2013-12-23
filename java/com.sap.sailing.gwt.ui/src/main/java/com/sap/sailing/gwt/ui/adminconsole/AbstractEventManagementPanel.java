package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.gwt.ui.client.AbstractRegattaPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public abstract class AbstractEventManagementPanel extends AbstractRegattaPanel {
    protected final TrackedRacesListComposite trackedRacesListComposite;
    private final List<RegattaDTO> availableRegattas;
    private final ListBox availableRegattasListBox;
    
    public AbstractEventManagementPanel(SailingServiceAsync sailingService, RegattaRefresher regattaRefresher,
            ErrorReporter errorReporter, RaceSelectionProvider raceSelectionProvider, StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, stringMessages);
        this.availableRegattas = new ArrayList<RegattaDTO>();
        
        this.availableRegattasListBox = new ListBox();
        this.availableRegattasListBox.ensureDebugId("AvailableRegattasListBox");
        
        // TrackedEventsComposite should exist in every *ManagementPanel. 
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                raceSelectionProvider, stringMessages, /* multiselection */ true);
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
    }
    
    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        this.trackedRacesListComposite.fillRegattas(regattas);
        RegattaDTO selectedRegatta = getSelectedRegatta();
        this.availableRegattas.clear();
        this.availableRegattasListBox.clear();
        this.availableRegattasListBox.addItem(this.stringMessages.defaultRegatta());
        List<RegattaDTO> regattasSortedByName = new ArrayList<RegattaDTO>(regattas);
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

}
