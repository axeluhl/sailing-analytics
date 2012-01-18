package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RacePlaceOrder;

public class EventDAO extends NamedDAO implements IsSerializable {
    public List<RegattaDAO> regattas;
    public List<CompetitorDAO> competitors;
    
    private String locations = null;

    public EventDAO() {
    }

    public EventDAO(String name, List<RegattaDAO> regattas, List<CompetitorDAO> competitors) {
        super(name);
        this.name = name;
        this.regattas = regattas;
        this.competitors = competitors;
    }

    /**
     * Returns the locations of the event as String.<br />
     * It has a format like this "Location1 -> Location2 -> ..." if the event has more than one location. Otherwhise it
     * returns just the name of the location.<br />
     * The locations are retrieved from the {@link RaceDAO RaceDAOs} in the {@link RegattaDAO RegattaDAOs}.
     * 
     * @return The locations of the event as String
     */
    public String getLocationAsString() {
        //TODO Finish the format of the location string
        if (locations == null) {
            for (RegattaDAO regattaDAO : regattas) {
                for (RaceDAO raceDAO : regattaDAO.races) {
                    RacePlaceOrder placeOrder = raceDAO.racePlaces;
                    if (placeOrder != null) {
                        locations += placeOrder.toString() + "; ";
                    }
                }
            }
        }

        return locations;
    }

    /**
     * @return The start date of the first {@link RaceDAO Race} in the first {@link RegattaDAO Regatta}, or
     *         <code>null</code> if the start date isn't set
     */
    public Date getStartDate() {
        return regattas.get(0).races.get(0).startOfRace;
    }
}
