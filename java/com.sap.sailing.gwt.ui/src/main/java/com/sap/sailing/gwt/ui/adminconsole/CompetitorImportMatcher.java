package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDescriptorDTO;

/**
 * Using to finding existing competitors which match with imported competitor.
 * Currently use the simple exact match comparison.
 * @author Alexander_Tatarinovich
 *
 */
public class CompetitorImportMatcher {
    //FIXME: Not sure about the amount of data. May be need to implement some cache for that.
    private final Iterable<CompetitorDTO> existingCompetitorDTOs;

    public CompetitorImportMatcher(Iterable<CompetitorDTO> existingCompetitors) {
        this.existingCompetitorDTOs = existingCompetitors;
    }

    public Set<CompetitorDTO> getMatchesCompetitors(CompetitorDescriptorDTO competitorDescriptor) {
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

    private boolean isEqual(CompetitorDescriptorDTO competitorDescriptor, CompetitorDTO existingCompetitor) {
        return Objects.equals(competitorDescriptor.getName(), existingCompetitor.getName())
                && Objects.equals(competitorDescriptor.getSailNumber(), existingCompetitor.getSailID())
                && compareCountryCode(competitorDescriptor, existingCompetitor);
    }

    private boolean compareCountryCode(CompetitorDescriptorDTO competitorDescriptor, CompetitorDTO existingCompetitor) {
        return Objects.equals(competitorDescriptor.getCountryName(), existingCompetitor.getCountryName())
                && Objects.equals(competitorDescriptor.getThreeLetterIocCountryCode(),
                        existingCompetitor.getThreeLetterIocCountryCode())
                && Objects.equals(competitorDescriptor.getTwoLetterIsoCountryCode(),
                        existingCompetitor.getTwoLetterIsoCountryCode());
    }
}
