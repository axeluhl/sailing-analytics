package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;

public class QuickRanksListBoxComposite extends Composite implements CompetitorSelectionChangeListener {
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private final List<QuickRankDAO> quickRankList;
    private final List<CompetitorDAO> competitorList;
    private final ListBox quickRankListBox;
    
    public QuickRanksListBoxComposite(CompetitorSelectionProvider competitorSelectionProvider) {
        this.competitorSelectionProvider = competitorSelectionProvider;
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
        quickRankList = new ArrayList<QuickRankDAO>();
        competitorList = new ArrayList<CompetitorDAO>();
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
    
    public void fillQuickRanks(List<QuickRankDAO> quickRanks, boolean isSorted) {
        Iterable<CompetitorDAO> oldSelection = competitorSelectionProvider.getSelectedCompetitors();
        quickRankList.clear();
        competitorList.clear();
        quickRankListBox.clear();
        
        for (QuickRankDAO quickRank: quickRanks) {
            quickRankList.add(quickRank);
            competitorList.add(quickRank.competitor);
        }
        if(!isSorted) {
            Collections.sort(quickRankList, new Comparator<QuickRankDAO>() {
                @Override
                public int compare(QuickRankDAO o1, QuickRankDAO o2) {
                    Integer rank1 = o1.rank;
                    Integer rank2 = o2.rank;
                    return rank1.compareTo(rank2);
                }

            });
        }
        int index = 0;
        for (QuickRankDAO quickRank : quickRankList) {
            quickRankListBox.addItem(toString(quickRank));
            if(competitorSelectionProvider.isSelected(quickRank.competitor))
                quickRankListBox.setItemSelected(index, true);
            index++;
        }
        // TODO update competitorSelectionProvider
    }

    public List<CompetitorDAO> getCompetitors() {
        return competitorList;            
    }
    
    private Iterable<CompetitorDAO> getSelectedCompetitors() {
        List<CompetitorDAO> result = new ArrayList<CompetitorDAO>();
        for (int i=0; i<quickRankListBox.getItemCount(); i++) {
            if (quickRankListBox.isItemSelected(i)) {
                result.add(quickRankList.get(i).competitor);
            }
        }
        return result;
    }

    protected String toString(QuickRankDAO quickRank) {
        return quickRank.rank + ". " + quickRank.competitor.name + " ("
                + quickRank.competitor.threeLetterIocCountryCode + ") in leg #" + (quickRank.legNumber + 1);
    }

    public ListBox getListBox() {
        return quickRankListBox;
    }

    @Override
    public void addedToSelection(CompetitorDAO competitor) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removedFromSelection(CompetitorDAO competitor) {
        // TODO Auto-generated method stub
        
    }

}
