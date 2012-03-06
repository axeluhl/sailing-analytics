package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EventDTO extends NamedDTO implements IsSerializable {
    public List<RegattaDTO> regattas;
    public List<CompetitorDTO> competitors;

    public EventDTO() {
    }

    public EventDTO(String name, List<RegattaDTO> regattas, List<CompetitorDTO> competitors) {
        super(name);
        this.name = name;
        this.regattas = regattas;
        this.competitors = competitors;
    }

    /**
     * @return The start date of the first {@link RaceDTO Race} in the first {@link RegattaDTO Regatta}, or
     *         <code>null</code> if the start date isn't set
     */
    public Date getStartDate() {
        return regattas.get(0).races.get(0).startOfRace;
    }
    
    /**
     * @return <code>true</code> if at least one race of the event is currently tracked, else it returns <code>false</code>
     */
    public boolean currentlyTracked() {
        boolean tracked = false;
        
        regattaLoop:
        for (RegattaDTO regatta : regattas) {
            for (RaceDTO race : regatta.races) {
                tracked = race.currentlyTracked;
                if (tracked) {
                    break regattaLoop;
                }
            }
        }
        
        return tracked;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((competitors == null) ? 0 : competitors.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventDTO other = (EventDTO) obj;
        if (competitors == null) {
            if (other.competitors != null)
                return false;
        } else if (!competitors.equals(other.competitors))
            return false;
        return true;
    }
    
}
