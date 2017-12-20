package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.pairinglist.CompetitionFormat;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;

public class PairingListImpl<Flight, Group, Competitor,Boat> implements PairingList<Flight, Group, Competitor,Boat>  {
    private final PairingListTemplate pairingListTemplate;
    private final CompetitionFormat<Flight, Group, Competitor> competitionFormat;
    private final List<Boat> boats;
    private final List<Competitor> competitors;
    
    /**
     * @param pList: pairing list with specific information of flights, groups and competitors
     * @param standardDev: describes quality of our pList (the lower the standardDev, the better the pairing list)
     */
    public PairingListImpl(PairingListTemplate template, CompetitionFormat<Flight, Group, Competitor> competitionFormat,ArrayList<Boat> boats) {
        this.pairingListTemplate = template;
        this.competitionFormat = competitionFormat;
        this.boats=boats;
        this.competitors=Util.asList(competitionFormat.getCompetitors());
    }
        
    @Override
    public Iterable<Pair<Competitor,Boat>> getCompetitors(Flight flight, Group group) {
        final int[][] competitorIndices = pairingListTemplate.getPairingListTemplate();
        final int flightIndex = Util.indexOf(competitionFormat.getFlights(), flight);
        final int groupIndex = Util.indexOf(competitionFormat.getGroups(flight), group);
        final int groupCount =Util.size(competitionFormat.getGroups(flight));
        final ArrayList<Integer> competitorIndicesInRace = new ArrayList<>();
        for (Integer integer : competitorIndices[flightIndex*groupCount+groupIndex]) {
            competitorIndicesInRace.add(integer);
        }
        final List<Pair<Competitor, Boat>> result = new ArrayList<>();
        for (int boatSlotInRace = 0; boatSlotInRace < competitorIndicesInRace.size(); boatSlotInRace++) {
            if (competitorIndicesInRace.get(boatSlotInRace) >= 0) {
                result.add(new Pair<Competitor, Boat>(competitors.get(competitorIndicesInRace.get(boatSlotInRace)),
                        boats.get(boatSlotInRace)));
            } else {
                result.add(new Pair<Competitor, Boat>(null, boats.get(boatSlotInRace)));
            }
        }
        return result;
    }
}
