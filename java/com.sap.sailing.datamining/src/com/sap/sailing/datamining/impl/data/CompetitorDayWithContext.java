package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasCompetitorDayContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;

public class CompetitorDayWithContext implements HasCompetitorDayContext {
    private final HasRaceOfCompetitorContext raceOfCompetitor;
    
    public CompetitorDayWithContext(HasRaceOfCompetitorContext raceOfCompetitor) {
        super();
        this.raceOfCompetitor = raceOfCompetitor;
    }

    @Override
    public int hashCode() {
        return 31 ^ getCompetitor().hashCode() ^ getDayAsISO().hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        final CompetitorDayWithContext other = (CompetitorDayWithContext) o;
        return getCompetitor().equals(other.getCompetitor()) &&
               getDayAsISO().equals(other.getDayAsISO());
    }
    
    @Override
    public HasRaceOfCompetitorContext getRaceOfCompetitor() {
        return raceOfCompetitor;
    }
    
    @Override
    public Competitor getCompetitor() {
        return raceOfCompetitor.getCompetitor();
    }

    @Override
    public String getDayAsISO() {
        return raceOfCompetitor.getTrackedRaceContext().getDayAsISO();
    }

}
