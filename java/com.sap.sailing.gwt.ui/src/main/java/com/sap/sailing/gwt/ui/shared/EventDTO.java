package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RacePlaceOrder;

public class EventDTO extends NamedDTO implements IsSerializable {
    public List<RegattaDTO> regattas;
    public List<CompetitorDTO> competitors;
    
    public String locations;

    public EventDTO() {
    }

    public EventDTO(String name, List<RegattaDTO> regattas, List<CompetitorDTO> competitors) {
        super(name);
        this.name = name;
        this.regattas = regattas;
        this.competitors = competitors;
        fillLocations();
    }

    private void fillLocations() {
        //TODO Finish the format of the location string
        StringBuilder b = new StringBuilder();
        boolean first = true;
        RacePlaceOrder previousOrder = null;
        locations = "";
        for (RegattaDTO regattaDTO : regattas) {
            for (RaceDTO raceDTO : regattaDTO.races) {
                RacePlaceOrder order = raceDTO.racePlaces;
                if (order != null) {
                    if (first) {
                        b.append(order.toString());
                        previousOrder = order;
                        first = false;
                    } else {
                        if (previousOrder.getFinishPlace().equals(order.getStartPlace())) {
                            b.append(" -> " + order.getStartPlace().getCountryCode() + ", " + order.getStartPlace().getName());
                        } else {
                            b.append("; " + order.toString());
                        }
                        previousOrder = order;
                    }
                }
            }
        }
        locations = b.toString().equals("") ? null : b.toString();
    }

    /**
     * @return The start date of the first {@link RaceDTO Race} in the first {@link RegattaDTO Regatta}, or
     *         <code>null</code> if the start date isn't set
     */
    public Date getStartDate() {
        return regattas.get(0).races.get(0).startOfRace;
    }
}
