package com.sap.sailing.gwt.home.communication.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

public class SimpleCompetitorWithIdDTO extends SimpleCompetitorDTO implements Result {

    private static final long serialVersionUID = -1236159499763467614L;

    private String idAsString;

    protected SimpleCompetitorWithIdDTO() {
    }

    @GwtIncompatible
    public SimpleCompetitorWithIdDTO(Competitor competitor) {
        super(competitor);
        this.idAsString = competitor.getId().toString();
    }

    @GwtIncompatible
    public SimpleCompetitorWithIdDTO(CompetitorWithBoatDTO competitor) {
        super(competitor);
        this.idAsString = competitor.getIdAsString();
    }

    public SimpleCompetitorWithIdDTO(String idAsString, String name, String shortInfo, 
            String twoLetterIsoCountryCode, String flagImageURL) {
        super(name, shortInfo, twoLetterIsoCountryCode, flagImageURL);
        this.idAsString = idAsString;
    }

    public String getIdAsString() {
        return idAsString;
    }

    @Override
    public int compareTo(SimpleCompetitorDTO obj) {
        int compareTo = super.compareTo(obj);
        return (compareTo == 0 && obj instanceof SimpleCompetitorWithIdDTO)
                ? this.idAsString.compareTo(((SimpleCompetitorWithIdDTO) obj).idAsString) : compareTo;
    }

}
