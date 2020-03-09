package com.sap.sailing.gwt.ui.client;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.sap.sailing.domain.common.AbstractSailNumberCanonicalizerAndMatcher;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public class SailNumberCanonicalizerAndMatcher extends AbstractSailNumberCanonicalizerAndMatcher<CompetitorDTO> {
    private static final RegExp sailIdPattern = RegExp.compile(sailIdRegexpPattern);

    @Override
    protected SailNumberMatch match(String sailId) {
        final SailNumberMatch result;
        if (sailId == null) {
            result = null;
        } else {
            final MatchResult m = sailIdPattern.exec(sailId.trim());
            if (m != null) {
                result = new SailNumberMatch(m.getGroup(1), m.getGroup(2));
            } else {
                result = null;
            }
        }
        return result;
    }
    
    @Override
    protected String getCompetitorIdentifyingText(CompetitorDTO competitor) {
        final String competitorIdentifyingText;
        if (competitor.hasBoat()) {
            competitorIdentifyingText = ((CompetitorWithBoatDTO) competitor).getSailID();
        } else {
            competitorIdentifyingText = competitor.getShortName();
        }
        return competitorIdentifyingText;
    }

    @Override
    protected String getThreeLetterIocCountryCode(CompetitorDTO competitor) {
        return competitor.getThreeLetterIocCountryCode();
    }
}
