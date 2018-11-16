package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.PairingListCreationException;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;

public class PairingListImpl<Flight, Group, Competitor,CompetitorAllocation> implements PairingList<Flight, Group, Competitor, CompetitorAllocation>  {
    private final PairingListTemplate pairingListTemplate;
    private final CompetitionFormat<Flight, Group, Competitor, CompetitorAllocation> competitionFormat;
    private final List<Competitor> competitors;
    
    /**
     * @param pList: pairing list with specific information of flights, groups and competitors
     * @param standardDev: describes quality of our pList (the lower the standardDev, the better the pairing list)
     */
    public PairingListImpl(PairingListTemplate template, CompetitionFormat<Flight, Group, Competitor, CompetitorAllocation> competitionFormat)
            throws PairingListCreationException {
        final int numberOfCompetitorAllocations = Util.size(competitionFormat.getCompetitorAllocation());
        if (numberOfCompetitorAllocations < competitionFormat.getMaxNumberOfCompetitorAllocationsNeeded()) {
            throw new PairingListCreationException("Too few competitor allocations ("+numberOfCompetitorAllocations+
                    "). "+competitionFormat.getMaxNumberOfCompetitorAllocationsNeeded()+" are needed.");
        }
        this.pairingListTemplate = template;
        this.competitionFormat = competitionFormat;
        this.competitors = Util.asList(competitionFormat.getCompetitors());
    }

    @Override
    public Iterable<Pair<Competitor, CompetitorAllocation>> getCompetitors(Flight flight, Group group) throws PairingListCreationException{
        if (competitors.isEmpty()) {
            throw new PairingListCreationException();
        }
        final int[][] competitorIndices = pairingListTemplate.getPairingListTemplate();
        final int flightIndex = Util.indexOf(competitionFormat.getFlights(), flight);
        final int groupIndex = Util.indexOf(competitionFormat.getGroups(flight), group);
        final int groupCount = Util.size(competitionFormat.getGroups(flight));
        final ArrayList<Integer> competitorIndicesInRace = new ArrayList<>();
        for (Integer integer : competitorIndices[flightIndex * groupCount + groupIndex]) {
            competitorIndicesInRace.add(integer);
        }
        final List<Pair<Competitor, CompetitorAllocation>> result = new ArrayList<>();
        for (int slot = 0; slot < competitorIndicesInRace.size(); slot++) {
            final Integer index = competitorIndicesInRace.get(slot);
            if (index >= 0) {
                result.add(new Pair<Competitor, CompetitorAllocation>(competitors.get(index),
                        Util.get(competitionFormat.getCompetitorAllocation(), slot)));
            } else {
                result.add(new Pair<Competitor, CompetitorAllocation>(null, 
                        Util.get(competitionFormat.getCompetitorAllocation(), slot)));
            }
        }
        return result;
    }
}
