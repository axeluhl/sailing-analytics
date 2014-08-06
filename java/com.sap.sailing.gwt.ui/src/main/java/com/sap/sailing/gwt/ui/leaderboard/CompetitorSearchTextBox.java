package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorRaceRankFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorSelectionProviderFilterContext;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorTotalRankFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSets;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSetsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSetsJsonDeSerializer;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFilterContext;
import com.sap.sailing.gwt.ui.client.shared.filter.SelectedCompetitorsFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.SelectedRaceFilterContext;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * A text box that belongs to a {@link LeaderboardPanel} and allows the user to search for competitors by sail number
 * and competitor name. When the user provides a non-empty search string, a new {@link Filter} for type {@link CompetitorDTO}
 * will be added that accepts competitors whose {@link CompetitorDTO#getSailID() sail number} or {@link CompetitorDTO#getName() name}
 * matches the user input. When the text box is emptied, 
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class CompetitorSearchTextBox extends HorizontalPanel implements KeyUpHandler, Filter<CompetitorDTO>, CompetitorSelectionChangeListener {
    private static final String ADVANCED_FILTER_BUTTON = "raceBoardNavigation-filterButton";
    private static final String ADVANCED_FILTER_BUTTON_FILTERED = "raceBoardNavigation-filterButton-filtered";

    private final static String LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY = "sailingAnalytics.raceBoard.competitorsFilterSets";

    private final TextBox searchTextBox;
    private final Button clearTextBoxButton;
    private final Button advancedSettingsButton;
    private final StringMessages stringMessages;
    private final AbstractListFilter<CompetitorDTO> filter;
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private String lastFilterSetNameWithoutThis;
    private final CompetitorsFilterSets competitorsFilterSets;
    private FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> lastActiveCompetitorFilterSet;
    private final LeaderboardFetcher leaderboardFetcher;
    private final RaceMap raceMap;
    private final RaceIdentifier selectedRaceIdentifier;

    public CompetitorSearchTextBox(final CompetitorSelectionProvider competitorSelectionProvider,
            final StringMessages stringMessages, RaceMap raceMap, LeaderboardFetcher leaderboardFetcher,
            RaceIdentifier selectedRaceIdentifier) {
        this.stringMessages = stringMessages;
        this.raceMap = raceMap;
        this.leaderboardFetcher = leaderboardFetcher;
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.competitorSelectionProvider = competitorSelectionProvider;
        CompetitorsFilterSets loadedCompetitorsFilterSets = loadCompetitorsFilterSets();
        if (loadedCompetitorsFilterSets != null) {
            competitorsFilterSets = loadedCompetitorsFilterSets;
            insertSelectedCompetitorsFilter(competitorsFilterSets);
        } else {
            competitorsFilterSets = createAndAddDefaultCompetitorsFilter();
            storeCompetitorsFilterSets(competitorsFilterSets);
        }
        
        filter = new AbstractListFilter<CompetitorDTO>() {
            @Override
            public Iterable<String> getStrings(CompetitorDTO competitor) {
                return Arrays.asList(competitor.getName().toLowerCase(), competitor.getSailID().toLowerCase());
            }
        };
        searchTextBox = new TextBox();
        searchTextBox.addKeyUpHandler(this);
        clearTextBoxButton = new Button("x");
        clearTextBoxButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                searchTextBox.setText("");
                onKeyUp(null);
            }
        });
        advancedSettingsButton = new Button("");
        advancedSettingsButton.addStyleName(ADVANCED_FILTER_BUTTON);
        advancedSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showEditCompetitorsFiltersDialog();
                    /* Code that would turn on the lastActiveCompetitorFilterSet again
                    if (lastActiveCompetitorFilterSet != null) {
                        competitorsFilterSets.setActiveFilterSet(lastActiveCompetitorFilterSet);
                        competitorSelectionProvider.setCompetitorsFilterSet(competitorsFilterSets.getActiveFilterSet());
                        updateCompetitorsFilterControlState(competitorsFilterSets);
                    }
                    */
                    /* Code that would remove / turn off the filter:
                    competitorsFilterSets.setActiveFilterSet(null);
                    competitorSelectionProvider.setCompetitorsFilterSet(competitorsFilterSets.getActiveFilterSet());
                    updateCompetitorsFilterControlState(competitorsFilterSets);
                    */
                }
            });
        add(searchTextBox);
        add(clearTextBoxButton);
        add(advancedSettingsButton);
    }

    public CompetitorsFilterSets getCompetitorsFilterSets() {
        return competitorsFilterSets;
    }

    /**
     * @param event ignored; may be <code>null</code>
     */
    @Override
    public void onKeyUp(KeyUpEvent event) {
        String newValue = searchTextBox.getValue();
        if (newValue.trim().isEmpty()) {
            removeSearchFilter();
        } else {
            ensureSearchFilterIsSet();
            competitorSelectionProvider.setCompetitorsFilterSet(competitorSelectionProvider.getCompetitorsFilterSet()); // 
        }
    }

    private void ensureSearchFilterIsSet() {
        if (competitorSelectionProvider.getCompetitorsFilterSet() == null || !Util.contains(competitorSelectionProvider.getCompetitorsFilterSet().getFilters(), this)) {
            FilterSet<CompetitorDTO, Filter<CompetitorDTO>> newFilterSetWithThis = new FilterSet<>(getName());
            if (competitorSelectionProvider.getCompetitorsFilterSet() != null) {
                for (Filter<CompetitorDTO> oldFilter : competitorSelectionProvider.getCompetitorsFilterSet()
                        .getFilters()) {
                    newFilterSetWithThis.addFilter(oldFilter);
                }
            }
            newFilterSetWithThis.addFilter(this);
            competitorSelectionProvider.setCompetitorsFilterSet(newFilterSetWithThis);
        }
    }

    private void removeSearchFilter() {
        if (competitorSelectionProvider.getCompetitorsFilterSet() != null
                && Util.contains(competitorSelectionProvider.getCompetitorsFilterSet().getFilters(), this)) {
            FilterSet<CompetitorDTO, Filter<CompetitorDTO>> newFilterSetWithThis = new FilterSet<>(lastFilterSetNameWithoutThis);
            for (Filter<CompetitorDTO> oldFilter : competitorSelectionProvider.getCompetitorsFilterSet().getFilters()) {
                if (oldFilter != this) {
                    newFilterSetWithThis.addFilter(oldFilter);
                }
            }
            competitorSelectionProvider.setCompetitorsFilterSet(newFilterSetWithThis);
        }
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        final String[] keywords = searchTextBox.getText().split(" ");
        final List<String> lowercaseKeywords = new ArrayList<>(keywords.length);
        for (String keyword : keywords) {
            lowercaseKeywords.add(keyword.toLowerCase());
        }
        return !Util.isEmpty(filter.applyFilter(lowercaseKeywords, Collections.singleton(competitor)));
    }

    @Override
    public String getName() {
        return stringMessages.competitorSearchFilter();
    }

    private void showEditCompetitorsFiltersDialog() {
        CompetitorsFilterSetsDialog competitorsFilterSetsDialog = new CompetitorsFilterSetsDialog(competitorsFilterSets,
                stringMessages, new DialogCallback<CompetitorsFilterSets>() {
            @Override
            public void ok(final CompetitorsFilterSets newCompetitorsFilterSets) {
                competitorsFilterSets.getFilterSets().clear();
                competitorsFilterSets.getFilterSets().addAll(newCompetitorsFilterSets.getFilterSets());
                competitorsFilterSets.setActiveFilterSet(newCompetitorsFilterSets.getActiveFilterSet());
                
                updateCompetitorsFilterContexts(newCompetitorsFilterSets);
                competitorSelectionProvider.setCompetitorsFilterSet(newCompetitorsFilterSets.getActiveFilterSet());
                updateCompetitorsFilterControlState(newCompetitorsFilterSets);
                storeCompetitorsFilterSets(newCompetitorsFilterSets);
             }

            @Override
            public void cancel() { 
            }
        });
        
        competitorsFilterSetsDialog .show();
    }

    private void insertSelectedCompetitorsFilter(CompetitorsFilterSets filterSet) {
        // selected competitors filter
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> selectedCompetitorsFilterSet = 
                new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(stringMessages.selectedCompetitors());
        selectedCompetitorsFilterSet.setEditable(false);
        SelectedCompetitorsFilter selectedCompetitorsFilter = new SelectedCompetitorsFilter();
        selectedCompetitorsFilter.setCompetitorSelectionProvider(competitorSelectionProvider);
        selectedCompetitorsFilterSet.addFilter(selectedCompetitorsFilter);
        
        filterSet.addFilterSet(0, selectedCompetitorsFilterSet);
    }
    
    private void updateCompetitorsFilterContexts(CompetitorsFilterSets filterSets) {
        for (FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> filterSet : filterSets.getFilterSets()) {
            for (Filter<CompetitorDTO> filter : filterSet.getFilters()) {
                if (leaderboardFetcher != null && filter instanceof LeaderboardFilterContext) {
                    ((LeaderboardFilterContext) filter).setLeaderboardFetcher(leaderboardFetcher);
                }
                if (filter instanceof SelectedRaceFilterContext) {
                    if (selectedRaceIdentifier != null) {
                        ((SelectedRaceFilterContext) filter).setSelectedRace(selectedRaceIdentifier);
                    }
                    if (raceMap != null) {
                        ((SelectedRaceFilterContext) filter).setQuickRankProvider(raceMap);
                    }
                }
                if (filter instanceof CompetitorSelectionProviderFilterContext) {
                    ((CompetitorSelectionProviderFilterContext) filter)
                            .setCompetitorSelectionProvider(competitorSelectionProvider);
                }
            }
        }
    }

    /**
     * Updates the competitor filter checkbox state by setting its check mark and updating its labal according to the
     * current filter selected
     */
    private void updateCompetitorsFilterControlState(CompetitorsFilterSets filterSets) {
        String competitorsFilterTitle = stringMessages.competitorsFilter();
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> activeFilterSet = filterSets.getActiveFilterSet();
        if (activeFilterSet != null) {
            if (lastActiveCompetitorFilterSet == null) {
                advancedSettingsButton.addStyleName(ADVANCED_FILTER_BUTTON_FILTERED);
            }
            lastActiveCompetitorFilterSet = activeFilterSet;
        } else {
            if (lastActiveCompetitorFilterSet != null) {
                advancedSettingsButton.removeStyleName(ADVANCED_FILTER_BUTTON_FILTERED);
            }
            lastActiveCompetitorFilterSet = null;
        }
        if (lastActiveCompetitorFilterSet != null) {
            advancedSettingsButton.setTitle(competitorsFilterTitle+" ("+lastActiveCompetitorFilterSet.getName()+")");
        } else {
            advancedSettingsButton.setTitle(competitorsFilterTitle);
        }
    }
    
   private CompetitorsFilterSets loadCompetitorsFilterSets() {
        CompetitorsFilterSets result = null;
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            try {
                String jsonAsLocalStore = localStorage.getItem(LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY);
                if (jsonAsLocalStore != null && !jsonAsLocalStore.isEmpty()) {
                    CompetitorsFilterSetsJsonDeSerializer deserializer = new CompetitorsFilterSetsJsonDeSerializer();
                    JSONValue value = JSONParser.parseStrict(jsonAsLocalStore);
                    if (value.isObject() != null) {
                        result = deserializer.deserialize((JSONObject) value);
                    }
                }
            } catch (Exception e) {
                // exception during loading of competitor filters from local storage
            }
        }
        return result;
    }

    private void storeCompetitorsFilterSets(CompetitorsFilterSets newCompetitorsFilterSets) {
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if(localStorage != null) {
            // delete old value
            localStorage.removeItem(LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY);
            
            // store the competiors filter set 
            CompetitorsFilterSetsJsonDeSerializer serializer = new CompetitorsFilterSetsJsonDeSerializer();
            JSONObject jsonObject = serializer.serialize(newCompetitorsFilterSets);
            localStorage.setItem(LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY, jsonObject.toString());
        }
    }
    
    private CompetitorsFilterSets createAndAddDefaultCompetitorsFilter() {
        CompetitorsFilterSets filterSets = new CompetitorsFilterSets();
        
        // 1. selected competitors filter
        insertSelectedCompetitorsFilter(filterSets);
        
        // 2. Top 50 competitors by race rank
        int maxRaceRank = 50;
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> topNRaceRankCompetitorsFilterSet = 
                new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(stringMessages.topNCompetitorsByRaceRank(maxRaceRank));
        CompetitorRaceRankFilter raceRankFilter = new CompetitorRaceRankFilter();
        raceRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
        raceRankFilter.setValue(maxRaceRank);
        topNRaceRankCompetitorsFilterSet.addFilter(raceRankFilter);
        filterSets.addFilterSet(topNRaceRankCompetitorsFilterSet);

        // 3. Top 50 competitors by total rank
        int maxTotalRank = 50;
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> topNTotalRankCompetitorsFilterSet =
                new FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>>(stringMessages.topNCompetitorsByTotalRank(maxTotalRank));
        CompetitorTotalRankFilter totalRankFilter = new CompetitorTotalRankFilter();
        totalRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
        totalRankFilter.setValue(50);
        topNTotalRankCompetitorsFilterSet.addFilter(totalRankFilter);
        filterSets.addFilterSet(topNTotalRankCompetitorsFilterSet);
        
        // set default active filter
        filterSets.setActiveFilterSet(topNRaceRankCompetitorsFilterSet);
        
        return filterSets;
    }
    
    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
        if (newFilterSet != null && !Util.contains(newFilterSet.getFilters(), this)) {
            lastFilterSetNameWithoutThis = newFilterSet.getName();
        }
        if (!Util.contains(newFilterSet.getFilters(), this)) {
            onKeyUp(null); // ensure that if the search box has text, this filter is in the current filter set
        } // else, this filter is part of the current filter set, and it will be so if and only if it shall be, e.g., because this object
        // has added itself as a filter
    }

    @Override public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {}
    @Override public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {}
    @Override public void addedToSelection(CompetitorDTO competitor) {}
    @Override public void removedFromSelection(CompetitorDTO competitor) {}
}
