package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.util.tools.shared.Md5Utils;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

public class RaceCompetitorSet {
    private final CompetitorSelectionProvider competitorSelection;
    
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
    private Iterable<String> idsAsStringOfCompetitorsParticipatingInRace;
    
    /**
     * A subset of the competitor selection's {@link CompetitorSelectionProvider#getAllCompetitors()} describing the
     * competitors participting in the context of a specific race, or <code>null</code> if this set hasn't been determined
     * yet, usually meaning that we have to default to all competitors of the regatta.<p>
     * 
     * When {@link #idsAsStringOfCompetitorsParticipatingInRace} is <code>null</code> then so is this field, and vice versa.
     */
    private Set<CompetitorDTO> competitorsParticipatingInRace;
    
    /**
     * The MD5 hash of the {@link #idsAsStringOfCompetitorsParticipatingInRace} where the competitors are first ordered
     * alphanumerically by their ID as String using {@link String#compareTo(String)}, then concatenating these strings
     * and converting to a byte[] using a UTF-8 encoding. May be <code>null</code> if it hasn't been computed before
     * or if {@link #idsAsStringOfCompetitorsParticipatingInRace} is still <code>null</code>.
     */
    private byte[] md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID;
    
    public RaceCompetitorSet(CompetitorSelectionProvider competitorSelection) {
        this.competitorSelection = competitorSelection;
        competitorSelection.addCompetitorSelectionChangeListener(new CompetitorSelectionChangeListener() {
            @Override
            public void removedFromSelection(CompetitorDTO competitor) {
                // we don't care about selection
            }
            
            @Override
            public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
                // filtering doesn't affect who we think participates in the race
            }
            
            @Override
            public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
                    FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
                // filtering doesn't affect who we think participates in the race
            }
            
            @Override
            public void competitorsListChanged(final Iterable<CompetitorDTO> competitors) {
                competitorsParticipatingInRace = computeCompetitorsFromIDs(competitors);
            }
            
            @Override
            public void addedToSelection(CompetitorDTO competitor) {
                // we don't care about selection
            }
        });
    }
    
    public void setIdsAsStringsOfCompetitorsInRace(Iterable<String> idsAsStringsOfCompetitorsInRace) throws UnsupportedEncodingException, IOException {
        this.idsAsStringOfCompetitorsParticipatingInRace = idsAsStringsOfCompetitorsInRace;
        competitorsParticipatingInRace = computeCompetitorsFromIDs(competitorSelection.getAllCompetitors());
        md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID = computeMD5(competitorsParticipatingInRace);
    }
    
    /**
     * Tries to locate the competitors described by the IDs in {@link #idsAsStringOfCompetitorsParticipatingInRace} in
     * <code>competitors</code> and returns them in a set. If not all competitors can be found, <code>null</code> is
     * returned instead.
     */
    private Set<CompetitorDTO> computeCompetitorsFromIDs(Iterable<CompetitorDTO> competitors) {
        final Map<String, CompetitorDTO> competitorsByIdAsString = new HashMap<>();
        for (CompetitorDTO c : competitors) {
            competitorsByIdAsString.put(c.getIdAsString(), c);
        }
        Set<CompetitorDTO> result = new HashSet<>();
        for (String id : idsAsStringOfCompetitorsParticipatingInRace) {
            CompetitorDTO c = competitorsByIdAsString.get(id);
            if (c == null) {
                result = null;
                break;
            } else {
                result.add(c);
            }
        }
        return result;
    }

    public byte[] getMd5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID() {
        return md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID;
    }

    private byte[] computeMD5(Iterable<CompetitorDTO> competitors) throws UnsupportedEncodingException, IOException {
        List<CompetitorDTO> l = new ArrayList<>();
        Util.addAll(competitors, l);
        Collections.sort(l, new Comparator<CompetitorDTO>() {
            @Override
            public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                return o1.getIdAsString().compareTo(o2.getIdAsString());
            }
        });
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (CompetitorDTO c : l) {
            bos.write(c.getIdAsString().getBytes("UTF-8"));
        }
        return Md5Utils.getMd5Digest(bos.toByteArray());
    }
}
