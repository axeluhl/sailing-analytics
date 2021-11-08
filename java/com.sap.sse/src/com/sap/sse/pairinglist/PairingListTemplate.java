package com.sap.sse.pairinglist;

import com.sap.sse.common.PairingListCreationException;

/**
 * Unfortunately there is no systematic way creating a pairing list, so our solution is based on the "trial and error"
 * principle. We generate about 100.000 pairing lists, comparing them to each other and returning the best.
 * 
 * Our quality attributes are:
 * 
 * 1. The distribution of how often a team competed against another team should be well-distributed. (2. Every team
 * should compete on different boats, so that no team is preferred.) (3. Although this point contradicts to point 2, )
 *
 */

public interface PairingListTemplate {
    <Flight, Group, Competitor, CompetitorAllocation> PairingList<Flight, Group, Competitor, CompetitorAllocation> createPairingList(
            CompetitionFormat<Flight, Group, Competitor, CompetitorAllocation> competitionFormat) throws PairingListCreationException;

    /**
     * The value corresponds to the quality of a pairing list template. The quality is calculated by the standard
     * deviation of values in association matrix.
     * 
     * @return double, stated as the standard deviation.
     */
    double getQuality();
    
    /**
     * The value corresponds to the quality of the distribution of competitors on the different boats. The quality is calculated by the standard
     * deviation of values in assignment matrix.
     * 
     * @return double, stated as the standard deviation.
     */
    double getBoatAssignmentsQuality();
    
    /**
     * This value represents the number of boat changes needed in the whole competition.
     * 
     * @return int, sum of boat changes.
     */
    int getBoatChanges();

    /**
     * Every row of this array represents a single group of a flight with competitors per group (competitors/groups) in
     * their columns. Competitors starts counting at zero. When calling this method the pairing list template is already
     * generated.
     * 
     * @return two dimensional int array:<br>
     *         <code>PairingListTemplate[flights*groups][competitors/groups]</code>
     */
    int[][] getPairingListTemplate();
}