package com.sap.sse.pairinglist;

/**
 * Unfortunately there is no systematic way creating a pairing list, so our solution is 
 * based on the "trial and error" principle. We generate about 100.000 pairing lists, comparing
 * them to each other and returning the best.
 * 
 * Our quality attributes are:
 * 
 *     1. The distribution of how often a team competed against another team should be well-distributed.
 *    (2. Every team should compete on different boats, so that no team is preferred.)
 *    (3. Although this point contradicts to point 2, ) 
 *
 */

public interface PairingListTemplate {
    <Flight, Group, Competitor> PairingList<Flight, Group, Competitor> createPairingList(
            CompetitionFormat<Flight, Group, Competitor> competitionFormat);
    
    /**
     * Returns the quality of a pairing list template.
     * 
     * @return quality of pairing list template, stated as the standard deviation,
     *         which is calculated by team association.
     */
    double getQuality();

    //TODO: Javadoc
    int[][] getPairingListTemplate();               
}