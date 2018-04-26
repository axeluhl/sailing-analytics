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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorRaceRankFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorSelectionProviderFilterContext;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorTotalRankFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSets;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSetsDialog;
import com.sap.sailing.gwt.ui.client.shared.filter.CompetitorsFilterSetsJsonDeSerializer;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterUIFactory;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFilterContext;
import com.sap.sailing.gwt.ui.client.shared.filter.QuickRankProvider;
import com.sap.sailing.gwt.ui.client.shared.filter.SelectedCompetitorsFilter;
import com.sap.sailing.gwt.ui.client.shared.filter.SelectedRaceFilterContext;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterResources.CompetitorFilterCss;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.BinaryOperator;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * A text box that belongs to a {@link ClassicLeaderboardPanel} and allows the user to search for competitors by sail number
 * and competitor name. When the user provides a non-empty search string, a new {@link Filter} for type {@link CompetitorDTO}
 * will be added that accepts competitors whose {@link CompetitorDTO#getSailID() sail number} or {@link CompetitorDTO#getName() name}
 * matches the user input. When the text box is emptied, 
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class CompetitorFilterPanel extends FlowPanel implements KeyUpHandler, FilterWithUI<CompetitorDTO>, CompetitorSelectionChangeListener {
    private final static String LOCAL_STORAGE_COMPETITORS_FILTER_SETS_KEY = "sailingAnalytics.raceBoard.competitorsFilterSets";
    private final static CompetitorFilterCss css = CompetitorFilterResources.INSTANCE.css();
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
    private final Button settingsButton;
    private final FlowPanel searchBoxPanel;

    public CompetitorFilterPanel(final CompetitorSelectionProvider competitorSelectionProvider,
            final StringMessages stringMessages, RaceMap raceMap, LeaderboardFetcher leaderboardFetcher,
            RaceIdentifier selectedRaceIdentifier) {
        css.ensureInjected();
        this.stringMessages = stringMessages;
        this.raceMap = raceMap;
        this.leaderboardFetcher = leaderboardFetcher;
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.setStyleName(css.competitorFilterContainer());
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
                final List<String> result = new ArrayList<>(Arrays.asList(competitor.getName().toLowerCase(), competitor.getShortName()));
                if (competitor.hasBoat()) {
                    result.add(((CompetitorWithBoatDTO) competitor).getSailID().toLowerCase());
                }
                return result;
            }
        };
        settingsButton = new Button();
        settingsButton.ensureDebugId("leaderboardSettingsButton");
        settingsButton.setTitle(stringMessages.settings());
        settingsButton.setStyleName(css.button());
        settingsButton.addStyleName(css.settingsButton());
        settingsButton.addStyleName(css.settingsButtonBackgroundImage());
        Button submitButton = new Button();
        submitButton.setStyleName(css.button());
        submitButton.addStyleName(css.searchButton());
        submitButton.addStyleName(css.searchButtonBackgroundImage());
        searchTextBox = new TextBox();
        searchTextBox.getElement().setAttribute("placeholder", stringMessages.searchCompetitorsBySailNumberOrName());
        searchTextBox.addKeyUpHandler(this);
        searchTextBox.setStyleName(css.searchInput());
        clearTextBoxButton = new Button();
        clearTextBoxButton.setStyleName(css.button());
        clearTextBoxButton.addStyleName(css.clearButton());
        clearTextBoxButton.addStyleName(css.clearButtonBackgroundImage());
        clearTextBoxButton.addStyleName(css.hiddenButton());
        clearTextBoxButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearSelection();
            }
        });
        advancedSettingsButton = new Button("");
        advancedSettingsButton.setStyleName(css.button());
        advancedSettingsButton.addStyleName(css.filterButton());
        advancedSettingsButton.setTitle(stringMessages.competitorsFilter());
        advancedSettingsButton.addStyleName(css.filterInactiveButtonBackgroundImage());
        advancedSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showEditCompetitorsFiltersDialog();
            }
        });
        searchBoxPanel = new FlowPanel();
        searchBoxPanel.setStyleName(css.searchBox());
        searchBoxPanel.add(submitButton);
        searchBoxPanel.add(searchTextBox);
        searchBoxPanel.add(clearTextBoxButton);
        add(searchBoxPanel);
        add(settingsButton);
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
            clearTextBoxButton.addStyleName(css.hiddenButton());
        } else {
            if (newValue.length() >= 2) {
                clearTextBoxButton.removeStyleName(css.hiddenButton());
                ensureSearchFilterIsSet();
                competitorSelectionProvider.setCompetitorsFilterSet(competitorSelectionProvider.getCompetitorsFilterSet()); // 
            }
        }
    }

    private void ensureSearchFilterIsSet() {
        if (competitorSelectionProvider.getCompetitorsFilterSet() == null || !Util.contains(competitorSelectionProvider.getCompetitorsFilterSet().getFilters(), this)) {
            FilterSet<CompetitorDTO, Filter<CompetitorDTO>> newFilterSetWithThis = new FilterSet<>(getName());
            if (competitorSelectionProvider.getCompetitorsFilterSet() != null) {
                for (Filter<CompetitorDTO> oldFilter : competitorSelectionProvider.getCompetitorsFilterSet().getFilters()) {
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
    
    public void clearSelection() {
        searchTextBox.setText("");
        clearTextBoxButton.addStyleName(css.hiddenButton());
        onKeyUp(null);
    }

    @Override
    public boolean matches(CompetitorDTO competitor) {
        final Iterable<String> lowercaseKeywords = Util
                .splitAlongWhitespaceRespectingDoubleQuotedPhrases(searchTextBox.getText().toLowerCase());
        return !Util.isEmpty(filter.applyFilter(lowercaseKeywords, Collections.singleton(competitor)));
    }

    @Override
    public String getName() {
        return stringMessages.competitorSearchFilter();
    }

    public void clearAllActiveFilters() {
        competitorsFilterSets.setActiveFilterSet(null);
        competitorSelectionProvider.clearAllFilters();
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
                competitorSelectionProvider.setCompetitorsFilterSet(newCompetitorsFilterSets.getActiveFilterSetWithGeneralizedType());
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
                advancedSettingsButton.removeStyleName(css.filterInactiveButtonBackgroundImage());
                advancedSettingsButton.addStyleName(css.filterActiveButtonBackgroundImage());
            }
            lastActiveCompetitorFilterSet = activeFilterSet;
        } else {
            if (lastActiveCompetitorFilterSet != null) {
                advancedSettingsButton.removeStyleName(css.filterActiveButtonBackgroundImage());
                advancedSettingsButton.addStyleName(css.filterInactiveButtonBackgroundImage());
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
        // 2. Top 30 competitors by race rank
        int maxRaceRank = 30;
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> topNRaceRankCompetitorsFilterSet = 
                new FilterSet<>(stringMessages.topNCompetitorsByRaceRank(maxRaceRank));
        CompetitorRaceRankFilter raceRankFilter = new CompetitorRaceRankFilter();
        raceRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
        raceRankFilter.setValue(maxRaceRank);
        topNRaceRankCompetitorsFilterSet.addFilter(raceRankFilter);
        filterSets.addFilterSet(topNRaceRankCompetitorsFilterSet);

        // 3. Top 30 competitors by total rank
        int maxTotalRank = 30;
        FilterSet<CompetitorDTO, FilterWithUI<CompetitorDTO>> topNTotalRankCompetitorsFilterSet =
                new FilterSet<>(stringMessages.topNCompetitorsByTotalRank(maxTotalRank));
        CompetitorTotalRankFilter totalRankFilter = new CompetitorTotalRankFilter();
        totalRankFilter.setOperator(new BinaryOperator<Integer>(BinaryOperator.Operators.LessThanEquals));
        totalRankFilter.setValue(50);
        topNTotalRankCompetitorsFilterSet.addFilter(totalRankFilter);
        filterSets.addFilterSet(topNTotalRankCompetitorsFilterSet);
        // set default active filter
        filterSets.setActiveFilterSet(topNRaceRankCompetitorsFilterSet);
        return filterSets;
    }
    
    /**
     * Provides the settings button shown in the panel for clients to add an event handler to it
     */
    public Button getSettingsButton() {
        return settingsButton;
    }
    
    /**
     * @return the {@link QuickRankProvider} if set or <code>null</code> if there is none
     */
    public QuickRankProvider getQuickRankProvider() {
        return this.raceMap;
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

    @Override
    public String validate(StringMessages stringMessages) {
        return null;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return getName();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return getName();
    }

    @Override
    public FilterWithUI<CompetitorDTO> copy() {
        return null;
    }

    @Override
    public FilterUIFactory<CompetitorDTO> createUIFactory() {
        return null;
    }

}
