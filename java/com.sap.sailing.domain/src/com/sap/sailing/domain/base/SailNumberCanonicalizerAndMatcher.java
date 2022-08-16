package com.sap.sailing.domain.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sap.sailing.domain.common.AbstractSailNumberCanonicalizerAndMatcher;

public class SailNumberCanonicalizerAndMatcher extends AbstractSailNumberCanonicalizerAndMatcher<Competitor> {
    private final static Pattern pattern = Pattern.compile(sailIdRegexpPattern);

    @Override
    protected SailNumberMatch match(String sailId) {
        final SailNumberMatch result;
        if (sailId == null) {
            result = null;
        } else {
            final Matcher matcher = pattern.matcher(sailId.trim());
            if (matcher.matches()) {
                result = new SailNumberMatch(matcher.group(1), matcher.group(2));
            } else {
                result = null;
            }
        }
        return result;
    }

    @Override
    protected String getCompetitorIdentifyingText(Competitor competitor) {
        final String competitorIdentifyingText;
        if (competitor.hasBoat()) {
            competitorIdentifyingText = ((CompetitorWithBoat) competitor).getBoat().getSailID();
        } else {
            competitorIdentifyingText = competitor.getShortName();
        }
        return competitorIdentifyingText;
    }

    @Override
    protected String getThreeLetterIocCountryCode(Competitor competitor) {
        return competitor.getNationality().getThreeLetterIOCAcronym();
    }
}
