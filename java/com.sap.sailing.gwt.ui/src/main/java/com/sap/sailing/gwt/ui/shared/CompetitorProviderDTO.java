package com.sap.sailing.gwt.ui.shared;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.sap.sse.security.shared.dto.NamedDTO;

public class CompetitorProviderDTO extends NamedDTO {
    private static final long serialVersionUID = -8911619278385485713L;

    private Map<String, Set<String>> hasCompetitorsForRegattasInEvent;

    public CompetitorProviderDTO() {
    }

    public CompetitorProviderDTO(String name, Map<String, Set<String>> hasCompetitorsForRegattasInEvent) {
        super(name);
        this.hasCompetitorsForRegattasInEvent = hasCompetitorsForRegattasInEvent;
    }

    public Map<String, Set<String>> getHasCompetitorsForRegattasInEvent() {
        return hasCompetitorsForRegattasInEvent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(hasCompetitorsForRegattasInEvent);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CompetitorProviderDTO other = (CompetitorProviderDTO) obj;
        return Objects.equals(hasCompetitorsForRegattasInEvent, other.hasCompetitorsForRegattasInEvent);
    }

}
