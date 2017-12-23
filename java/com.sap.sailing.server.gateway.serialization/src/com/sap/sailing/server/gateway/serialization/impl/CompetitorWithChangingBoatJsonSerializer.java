package com.sap.sailing.server.gateway.serialization.impl;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sse.common.Color;

/**
 * This is a workaround for the serialization of a competitor with changing boats.
 * The class should be removed after bug https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2822 is resolved.
 * @author Frank
 *
 */
public class CompetitorWithChangingBoatJsonSerializer extends CompetitorJsonSerializer {
    private final RaceDefinition race;
    
    public CompetitorWithChangingBoatJsonSerializer() {
        this(null, null, null);
    }

    public CompetitorWithChangingBoatJsonSerializer(RaceDefinition race, JsonSerializer<Team> teamJsonSerializer, JsonSerializer<Boat> boatSerializer) {
        super(teamJsonSerializer,  boatSerializer); 
        this.race = race;
    }

    @Override
    protected Color getColor(Competitor competitor) {
        if (race != null) {
            Boat boatOfCompetitor = race.getBoatOfCompetitor(competitor);
            if (boatOfCompetitor != null) {
                return boatOfCompetitor.getColor();
            }
        }
        return super.getColor(competitor);
    }
    
    @Override
    protected Boat getBoat(Competitor competitor) {
        if (race != null) {
            Boat boatOfCompetitor = race.getBoatOfCompetitor(competitor);
            if (boatOfCompetitor != null) {
                return boatOfCompetitor;
            }
        }
        return super.getBoat(competitor);
    }
}
