package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.AbstractListFilter;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;

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
    private final TextBox searchTextBox;
    private final Button clearTextBoxButton;
    private final Button filterTop30Button;
    private final Button advancedSettingsButton;
    private final StringMessages stringMessages;
    private final AbstractListFilter<CompetitorDTO> filter;
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private String lastFilterSetNameWithoutThis;
    
    public CompetitorSearchTextBox(final CompetitorSelectionProvider competitorSelectionProvider, final StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.competitorSelectionProvider = competitorSelectionProvider;
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
        filterTop30Button = new Button("T30");
        advancedSettingsButton = new Button("");
        advancedSettingsButton.addStyleName("raceBoardNavigation-filterButton");
        advancedSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Not yet implemented");
            }
        });
        add(searchTextBox);
        add(clearTextBoxButton);
        add(filterTop30Button);
        add(advancedSettingsButton);
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
