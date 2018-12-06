package com.sap.sailing.gwt.home.desktop.places.event.regatta.leaderboardtab;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sse.common.Util;

public class RegattaLeaderboardPlace extends AbstractEventRegattaPlace {

    private final Set<String> selectedCompetitors = new HashSet<>();

    public RegattaLeaderboardPlace(String id, String regattaId, Set<String> selectedCompetitors) {
        super(id, regattaId);
        if (selectedCompetitors != null) {
            this.selectedCompetitors.addAll(selectedCompetitors);
        }
    }

    public RegattaLeaderboardPlace(String id, String regattaId) {
        this(id, regattaId, null);
    }

    public RegattaLeaderboardPlace(EventContext context, Set<String> selectedCompetitors) {
        super(context);
        if (selectedCompetitors != null) {
            this.selectedCompetitors.addAll(selectedCompetitors);
        }
    }

    public RegattaLeaderboardPlace(EventContext context) {
        this(context, null);
    }

    @Override
    public AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx) {
        return new RegattaLeaderboardPlace(ctx, null);
    }

    public Set<String> getSelectedCompetitors() {
        return selectedCompetitors;
    }

    @Prefix(PlaceTokenPrefixes.RegattaLeaderboard)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<RegattaLeaderboardPlace> {

        /** short parameter name to decrease total URL length */
        private static final String PARAM_SELECTED_COMPETITORS = "sc";

        @Override
        protected Map<String, Set<String>> getParameters(RegattaLeaderboardPlace place) {
            Map<String, Set<String>> map = super.getParameters(place);
            place.getSelectedCompetitors().forEach(value -> Util.addToValueSet(map, PARAM_SELECTED_COMPETITORS, value));
            return map;
        }

        @Override
        protected RegattaLeaderboardPlace getRealPlace(EventContext context, Map<String, Set<String>> parameters) {
            return new RegattaLeaderboardPlace(context, parameters.get(PARAM_SELECTED_COMPETITORS));
        }

        @Override
        protected RegattaLeaderboardPlace getRealPlace(EventContext context) {
            // Is not called since the calling method is overridden above
            return null;
        }
    }
}
