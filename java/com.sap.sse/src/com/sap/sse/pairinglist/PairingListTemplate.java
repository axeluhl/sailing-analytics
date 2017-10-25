package com.sap.sse.pairinglist;

public interface PairingListTemplate {
    <Flight, Group, Competitor> PairingList<Flight, Group, Competitor> createPairingList(
            CompetitionFormat<Flight, Group, Competitor> competitionFormat);
    
    double getQuality();

    int[][] getPairingListTemplate();

}