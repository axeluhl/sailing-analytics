package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sse.common.Util;

/**
 * Used to find existing competitors which match with imported competitor. Currently use the simple exact match
 * comparison.
 * 
 * @author Alexander Tatarinovich
 *
 */
public class CompetitorImportMatcher {
    //FIXME: Not sure about the amount of data. May be need to implement some cache for that.
    private final Iterable<CompetitorDTO> existingCompetitorDTOs;

    public CompetitorImportMatcher(Iterable<CompetitorDTO> existingCompetitors) {
        this.existingCompetitorDTOs = existingCompetitors;
    }

    public Set<CompetitorDTO> getMatchesCompetitors(CompetitorDescriptor competitorDescriptor) {
        Set<CompetitorDTO> matchesCompetitor = new HashSet<>();
        if (competitorDescriptor == null) {
            return matchesCompetitor;
        }
        for (CompetitorDTO existingCompetitor : existingCompetitorDTOs) {
            if (isEqual(competitorDescriptor, existingCompetitor)) {
                matchesCompetitor.add(existingCompetitor);
            }
        }
        return matchesCompetitor;
    }

    private boolean isEqual(CompetitorDescriptor competitorDescriptor, CompetitorDTO existingCompetitor) {
        return Util.equalsWithNull(competitorDescriptor.getName(), existingCompetitor.getName(), /* ignoreCase */ true)
                && (!existingCompetitor.hasBoat() ||
                        Util.equalsWithNull(removeSpaces(competitorDescriptor.getSailNumber()),
                                            removeSpaces(((CompetitorWithBoatDTO) existingCompetitor).getSailID()), /* ignoreCase */ true))
                && compareCountryCode(competitorDescriptor, existingCompetitor);
    }
    
    private String removeSpaces(String s) {
        return s==null?null:s.replace(" ", "").replace("\t", "");
    }

    private boolean compareCountryCode(CompetitorDescriptor competitorDescriptor, CompetitorDTO existingCompetitor) {
        return Util.equalsWithNull(competitorDescriptor.getCountryCode() == null ? null : competitorDescriptor.getCountryCode().getThreeLetterIOCCode(),
                        existingCompetitor.getThreeLetterIocCountryCode(), /* ignoreCase */ true);
    }
}
