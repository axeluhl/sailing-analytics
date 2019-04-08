package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RaceCompetitorIdsAsStringWithMD5Hash;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceCompetitorSelectionProvider;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

/**
 * As an extension of {@link RaceCompetitorIdsAsStringWithMD5Hash}, adds the link with a
 * {@link CompetitorSelectionProvider} and allows its clients to obtain the {@link CompetitorWithBoatDTO} objects for those
 * competitors participating in the race under consideration.
 * <p>
 * 
 * The implementation caches the {@link CompetitorWithBoatDTO}s and keeps this cache up to date by
 * {@link CompetitorSelectionProvider#addCompetitorSelectionChangeListener(CompetitorSelectionChangeListener)}
 * listening} on the competitor selection provider for changes. When either the set of competitors for the race
 * {@link #setIdsAsStringsOfCompetitorsInRace(Iterable) changes} or the set of competitors
 * {@link CompetitorSelectionChangeListener#competitorsListChanged(Iterable) changes}, the {@link CompetitorWithBoatDTO}
 * collection is re-calculated.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceCompetitorSet extends RaceCompetitorIdsAsStringWithMD5Hash {
    private static final long serialVersionUID = 3357742414149799988L;

    private RaceCompetitorSelectionProvider competitorSelection;
    
    /**
     * A subset of the competitor selection's {@link CompetitorSelectionProvider#getAllCompetitors()} describing the
     * competitors participting in the context of a specific race, or <code>null</code> if this set hasn't been determined
     * yet, usually meaning that we have to default to all competitors of the regatta.<p>
     * 
     * When {@link #idsAsStringOfCompetitorsParticipatingInRace} is <code>null</code> then so is this field, and vice versa.
     */
    private Iterable<CompetitorDTO> competitorsParticipatingInRace;

    private Set<CompetitorsForRaceDefinedListener> competitorsForRaceDefinedListeners;
    
    /**
     * Such listeners are notified whenever the response to {@link #getCompetitorsParticipatingInRace()} changes.
     */
    public static interface CompetitorsForRaceDefinedListener {
        void competitorsForRaceDefined(Iterable<CompetitorDTO> competitors);
    }
    
    RaceCompetitorSet() {} // for GWT serialization only
    
    /**
     * Starts out using {@link CompetitorSelectionProvider#getAllCompetitors() all regatta competitors} as response for
     * {@link #getCompetitorsParticipatingInRace()}. Only after a call to
     * {@link #setIdsAsStringsOfCompetitorsInRace(Iterable)} has been received may this set become adjusted to the
     * actual subset participating in the race.
     */
    public RaceCompetitorSet(RaceCompetitorSelectionProvider competitorSelection) {
        super();
        this.competitorsForRaceDefinedListeners = new HashSet<>();
        this.competitorSelection = competitorSelection;
        this.competitorsParticipatingInRace = competitorSelection.getAllCompetitors();
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
                notifyListeners();
            }
            
            @Override
            public void addedToSelection(CompetitorDTO competitor) {
                // we don't care about selection
            }
        });
    }
    

    public void addCompetitorsForRaceDefinedListener(CompetitorsForRaceDefinedListener listener) {
        competitorsForRaceDefinedListeners.add(listener);
    }
    
    public void removeCompetitorsForRaceDefinedListener(CompetitorsForRaceDefinedListener listener) {
        competitorsForRaceDefinedListeners.remove(listener);
    }

    public void setIdsAsStringsOfCompetitorsInRace(Set<String> idsAsStringsOfCompetitorsInRace) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        super.setIdsAsStringsOfCompetitorsInRace(idsAsStringsOfCompetitorsInRace);
        competitorsParticipatingInRace = computeCompetitorsFromIDs(competitorSelection.getAllCompetitors());
        notifyListeners();
    }
    
    private void notifyListeners() {
        for (final CompetitorsForRaceDefinedListener listener : competitorsForRaceDefinedListeners) {
            listener.competitorsForRaceDefined(competitorsParticipatingInRace);
        }
    }

    public Iterable<CompetitorDTO> getCompetitorsParticipatingInRace() {
        return competitorsParticipatingInRace;
    }

    /**
     * Tries to locate the competitors described by the IDs in {@link #idsAsStringOfCompetitorsParticipatingInRace} in
     * <code>competitors</code> and returns them in a set. The subset of competitors found this way is returned.
     * Note that due to the possibility of suppressing competitors it is possible that competitors are listed
     * as entries in the race but cannot be resolved in the leaderboard's competitors which does not contain
     * those being suppressed.
     */
    private Set<CompetitorDTO> computeCompetitorsFromIDs(Iterable<CompetitorDTO> competitors) {
        Set<CompetitorDTO> result;
        if (getIdsOfCompetitorsParticipatingInRaceAsStrings() == null) {
            result = null;
        } else {
            final Map<String, CompetitorDTO> competitorsByIdAsString = new HashMap<>();
            for (CompetitorDTO c : competitors) {
                competitorsByIdAsString.put(c.getIdAsString(), c);
            }
            result = new HashSet<>();
            for (String id : getIdsOfCompetitorsParticipatingInRaceAsStrings()) {
                CompetitorDTO c = competitorsByIdAsString.get(id);
                if (c != null) {
                    result.add(c);
                }
            }
        }
        return result;
    }
}
