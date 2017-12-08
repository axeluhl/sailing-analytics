package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util;
import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;

public class PairingListImpl<Flight, Group, Competitor> implements PairingList<Flight, Group, Competitor>  {
    private final PairingListTemplate pairingListTemplate;
    private final CompetitionFormat<Flight, Group, Competitor> competitionFormat;
    
    /**
     * @param pList: pairing list with specific information of flights, groups and competitors
     * @param standardDev: describes quality of our pList (the lower the standardDev, the better the pairing list)
     */
    public PairingListImpl(PairingListTemplate template, CompetitionFormat<Flight, Group, Competitor> competitionFormat) {
        this.pairingListTemplate = template;
        this.competitionFormat = competitionFormat;
    }
        
    @Override
    public Iterable<Competitor> getCompetitors(Flight flight, Group group) {
        final int[][] competitorIndices = pairingListTemplate.getPairingListTemplate();
        final int flightIndex = Util.indexOf(competitionFormat.getFlights(), flight);
        final int groupIndex = Util.indexOf(competitionFormat.getGroups(flight), group);
        final int groupCount =Util.size(competitionFormat.getGroups(flight));
        final ArrayList<Integer> competitorIndicesInRace = new ArrayList<>();
        /*
        System.arraycopy(competitorIndices[flightIndex], groupIndex * competitorIndicesInRace.length,
                competitorIndicesInRace, 0, competitorIndicesInRace.length);*/
        for (Integer integer : competitorIndices[flightIndex*groupCount+groupIndex]) {
            competitorIndicesInRace.add(integer);
        }
        
        final List<Competitor> result = new ArrayList<>();
        for (final int competitorIndexInRace : competitorIndicesInRace) {
            if (competitorIndexInRace > -1) {
                result.add(Util.get(competitionFormat.getCompetitors(), competitorIndexInRace));
            } else {
                result.add(null);
            }
        }
        return result;
    }
}
