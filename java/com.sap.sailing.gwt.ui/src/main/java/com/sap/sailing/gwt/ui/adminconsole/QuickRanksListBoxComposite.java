package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;

/**
 * Shows a lean view of a race's competitors' ranks in a list. The list shows a subset of the race's competitors because
 * not at all times may the ranks for all competitors be known. The list selection is tied to a
 * {@link CompetitorSelectionProvider}. Regarding selection propagation from this view to the selection provider
 * there is a minor glitch: since this view may show only a subset of all available competitors, this view's selection
 * may also be just a subset of the current selection. If the selection is changed through this view, other previously
 * selected competitors that are not shown in this view will be deselected.
 * 
 * @author Frank Mittag (C5163874), Axel Uhl (d043530)
 * 
 */
public class QuickRanksListBoxComposite extends Composite implements CompetitorSelectionChangeListener {
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private final List<QuickRankDTO> quickRankList;
    private final List<CompetitorDTO> competitorList;
    private final ListBox quickRankListBox;
    
    public QuickRanksListBoxComposite(CompetitorSelectionProvider competitorSelectionProvider) {
        this.competitorSelectionProvider = competitorSelectionProvider;
        competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
        quickRankListBox = new ListBox(competitorSelectionProvider.hasMultiSelection());
        quickRankListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onLocalSelectionChanged();
            }
        });
        quickRankListBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onLocalSelectionChanged();
            }
        });
        quickRankList = new ArrayList<QuickRankDTO>();
        competitorList = new ArrayList<CompetitorDTO>();
        VerticalPanel vp = new VerticalPanel();
        vp.add(quickRankListBox);
        // All composites must call initWidget() in their constructors.
        initWidget(vp);
    }

    /**
     * propagates changes in the local selection to the selection provider
     */
    private void onLocalSelectionChanged() {
        competitorSelectionProvider.setSelection(getSelectedCompetitors(), this);
    }
    
    /**
     * @param quickRanks list of quick ranks, ordered by ascending rank
     */
    public void fillQuickRanks(List<QuickRankDTO> quickRanks) {
        quickRankList.clear();
        competitorList.clear();
        quickRankListBox.clear();
        for (QuickRankDTO quickRank: quickRanks) {
            quickRankList.add(quickRank);
            competitorList.add(quickRank.competitor);
        }
        int index = 0;
        for (QuickRankDTO quickRank : quickRankList) {
            quickRankListBox.addItem(toString(quickRank));
            if (competitorSelectionProvider.isSelected(quickRank.competitor)) {
                quickRankListBox.setItemSelected(index, true);
            }
            index++;
        }
    }

    private Iterable<CompetitorDTO> getSelectedCompetitors() {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (int i=0; i<quickRankListBox.getItemCount(); i++) {
            if (quickRankListBox.isItemSelected(i)) {
                result.add(quickRankList.get(i).competitor);
            }
        }
        return result;
    }

    protected String toString(QuickRankDTO quickRank) {
        return quickRank.rank + ". " + quickRank.competitor.name + " ("
                + quickRank.competitor.threeLetterIocCountryCode + ") in leg #" + (quickRank.legNumber + 1);
    }

    public ListBox getListBox() {
        return quickRankListBox;
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        int pos = quickRankList.indexOf(competitor);
        if (pos >= 0) {
            quickRankListBox.setItemSelected(pos, true);
        }
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        int pos = quickRankList.indexOf(competitor);
        if (pos >= 0) {
            quickRankListBox.setItemSelected(pos, false);
        }
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        //TODO what to do here
    }

}
