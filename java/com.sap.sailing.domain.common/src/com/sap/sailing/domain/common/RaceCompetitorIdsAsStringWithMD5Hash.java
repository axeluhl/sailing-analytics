package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sap.sse.common.Util;

/**
 * Manages the set of competitors participating in a specific race. This is not trivial for a {@link RaceBoardPanel}
 * because it is provided with the {@link CompetitorSelectionProvider} that is also used by the {@link LeaderboardPanel}
 * and therefore has all competitors of the entire regatta, not just those of the race that the race board is showing.
 * <p>
 * 
 * The back-end knows the race-specific set of competitors and can tell the client. However, we'd like to keep the
 * network traffic required to keep this information up to date at a minimum. The key idea is to use an MD5 hash across
 * the ordered race's competitor IDs and send that with each {@link GetRaceMapDataAction} request. The server can then
 * compare this hash with the current race's competitor set and send an update if and only if there is a difference
 * detected. This will, for the most part, reduce the communication to an initial request by the client as long as the
 * race's competitors are not known, and the additional MD5 hash being sent with each request.
 * <p>
 * 
 * Objects of this type start out with an "unknown" race competitor set. When the
 * {@link #getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID() MD5 hash} for the currently
 * known race competitors is asked, <code>null</code> will initially be returned. When the client has received the race
 * competitors the client is expected to call {@link #setIdsAsStringsOfCompetitorsInRace(Iterable)}. If the argument to
 * this call was not <code>null</code>,
 * {@link #getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID()} will from then on return
 * the MD5 hash of the competitor IDs in order.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceCompetitorIdsAsStringWithMD5Hash implements Serializable {
    private static final long serialVersionUID = 6881670366184539259L;

    /**
     * The {@link #competitorSelection} manages the competitors for the entire leaderboard which may be a
     * superset of the competitors participating in the race shown by this map. For certain operations,
     * however, it is useful to know exactly the competitors participating in the race shown by this map.
     * For example, boat positions should only be requested from the server for those competitors, not all
     * in the regatta, particularly when racing in split fleets.<p>
     * 
     * To keep this set consistent, the client sends a secure hash of this set to the server where the hash
     * will be compared with the hash across the set of competitor IDs as string.<p>
     * 
     * If this field is <code>null</code>, nothing is known yet about the competitor-to-race assignment.
     */
    private Set<String> idsAsStringOfCompetitorsParticipatingInRace;
    
    /**
     * The MD5 hash of the {@link #idsAsStringOfCompetitorsParticipatingInRace} where the competitors are first ordered
     * alphanumerically by their ID as String using {@link String#compareTo(String)}, then concatenating these strings
     * and converting to a byte[] using a UTF-8 encoding. May be <code>null</code> if it hasn't been computed before
     * or if {@link #idsAsStringOfCompetitorsParticipatingInRace} is still <code>null</code>.
     */
    private byte[] md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID;
    
    /**
     * Starts out with an unknown set of race competitors. The {@link #getIdsOfCompetitorsParticipatingInRaceAsStrings()}
     * and the {@link #getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID()} will return
     * <code>null</code> until a call to {@link #setIdsAsStringsOfCompetitorsInRace(Iterable)} with a non-
     * <code>null</code> argument has been performed.
     */
    public RaceCompetitorIdsAsStringWithMD5Hash() {
    }

    /**
     * Immediately calls {@link #setIdsAsStringsOfCompetitorsInRace(Set)} with the argument provided. If that
     * was <code>null</code> then this has the same effect as calling {@link #RaceCompetitorIdsAsStringWithMD5Hash()}.
     */
    public RaceCompetitorIdsAsStringWithMD5Hash(Set<String> idsAsStringOfCompetitorsParticipatingInRace)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this.setIdsAsStringsOfCompetitorsInRace(idsAsStringOfCompetitorsParticipatingInRace);
    }

    public void setIdsAsStringsOfCompetitorsInRace(Set<String> idsAsStringsOfCompetitorsInRace) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        this.idsAsStringOfCompetitorsParticipatingInRace = idsAsStringsOfCompetitorsInRace;
        md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID = computeMD5(idsAsStringsOfCompetitorsInRace);
    }
    
    public byte[] getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID() {
        return md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID;
    }
    
    public Set<String> getIdsOfCompetitorsParticipatingInRaceAsStrings() {
        return idsAsStringOfCompetitorsParticipatingInRace;
    }

    private byte[] computeMD5(Iterable<String> idsAsStringsOfCompetitorsInRace) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        List<String> l = new ArrayList<>();
        Util.addAll(idsAsStringsOfCompetitorsInRace, l);
        Collections.sort(l);
        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (String c : l) {
            md5.update(c.getBytes("UTF-8"));
        }
        return md5.digest();
    }
}
