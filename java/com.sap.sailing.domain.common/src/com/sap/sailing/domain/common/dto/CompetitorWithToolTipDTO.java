package com.sap.sailing.domain.common.dto;

import java.util.Objects;

public class CompetitorWithToolTipDTO {

    private CompetitorDTO competitor;
    private String toolTipMessage;

    public CompetitorWithToolTipDTO() {
    }

    public CompetitorWithToolTipDTO(CompetitorDTO competitor, String toolTipMessage) {
        this.competitor = competitor;
        this.toolTipMessage = toolTipMessage;
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public String getToolTipMessage() {
        return toolTipMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(competitor, toolTipMessage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CompetitorWithToolTipDTO other = (CompetitorWithToolTipDTO) obj;
        return Objects.equals(competitor, other.getCompetitor())
                && Objects.equals(toolTipMessage, other.getToolTipMessage());
    }
}
