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
        this.availableRegattasListBox.ensureDebugId("AvailableRegattas");
        // TrackedEventsComposite should exist in every *ManagementPanel. 
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                raceSelectionProvider, stringMessages, /* multiselection */ true);
    }
    
    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        this.trackedRacesListComposite.fillRegattas(regattas);
        RegattaDTO selectedRegatta = getSelectedRegatta();
        this.availableRegattas.clear();
        this.availableRegattasListBox.clear();
        this.availableRegattasListBox.addItem(this.stringMessages.noRegatta());
        List<RegattaDTO> regattasSortedByName = new ArrayList<RegattaDTO>(regattas);
        Collections.sort(regattasSortedByName, new Comparator<RegattaDTO>() {
            @Override
            public int compare(RegattaDTO o1, RegattaDTO o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        for (RegattaDTO regatta : regattasSortedByName) {
            this.availableRegattas.add(regatta);
            this.availableRegattasListBox.addItem(regatta.name);
            if(selectedRegatta != null && selectedRegatta.name.equals(regatta.name)) {
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
            String itemText = this.availableRegattasListBox.getItemText(selIndex);
            for (RegattaDTO regatta : this.availableRegattas) {
                if (regatta.name.equals(itemText)) {
                    result = regatta;
                    break;
                }
            }
        }
        return result;
    }

}
