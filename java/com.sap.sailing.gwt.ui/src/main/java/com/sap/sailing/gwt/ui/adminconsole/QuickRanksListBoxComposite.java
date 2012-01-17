package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class QuickRanksListBoxComposite extends Composite implements CompetitorSelectionProvider {
    private final Set<CompetitorSelectionChangeListener> competitorSelectionChangeListeners;
    private final List<QuickRankDAO> quickRankList;
    private final List<CompetitorDAO> competitorList;
    private final ListBox quickRankListBox;
    private final boolean hasMultiSelection;
    
    public QuickRanksListBoxComposite(final boolean hasMultiSelection) {
        this.competitorSelectionChangeListeners = new HashSet<CompetitorSelectionChangeListener>();
        this.hasMultiSelection = hasMultiSelection;
        quickRankListBox = new ListBox(hasMultiSelection);

        quickRankListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onSelectionChanged();
            }
        });
        quickRankListBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onSelectionChanged();
            }
        });

        quickRankList = new ArrayList<QuickRankDAO>();
        competitorList = new ArrayList<CompetitorDAO>();
        VerticalPanel vp = new VerticalPanel();
        vp.add(quickRankListBox);
        
        // All composites must call initWidget() in their constructors.
        initWidget(vp);
    }

    private void onSelectionChanged()
    {
        List<CompetitorDAO> selectedCompetitors = null;
        if(hasMultiSelection) {
            selectedCompetitors = getSelectedCompetitors();
        } else { 
            CompetitorDAO selectedCompetitor = getSelectedCompetitor();
            if (selectedCompetitor != null) {
                selectedCompetitors = new ArrayList<CompetitorDAO>();
                selectedCompetitors.add(selectedCompetitor);
            }
        }
        fireCompetitorSelectionChanged(selectedCompetitors);
    }
    
    public void fillQuickRanks(List<QuickRankDAO> quickRanks, boolean isSorted) {
        List<CompetitorDAO> oldSelection = getSelectedCompetitors();

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
            if(oldSelection.contains(quickRank.competitor))
                quickRankListBox.setItemSelected(index, true);
            index++;
        }
        fireCompetitorSelectionChanged(getSelectedCompetitors());
    }

    public List<CompetitorDAO> getCompetitors() {
        return competitorList;            
    }
    
    private CompetitorDAO getSelectedCompetitor() {
        int i = quickRankListBox.getSelectedIndex();
        CompetitorDAO result = null;
        if (i >= 0) {
            result = quickRankList.get(i).competitor;
        }
        return result;
    }

    @Override
    public List<CompetitorDAO> getSelectedCompetitors() {
        int i=0;
        List<CompetitorDAO> result = new ArrayList<CompetitorDAO>();
        for (QuickRankDAO quickRank: quickRankList) {
            if (quickRankListBox.isItemSelected(i)) {
                result.add(quickRank.competitor);
            }
            i++;
        }
        System.out.println("Anzahl Selected competitors: " + result.size());
        return result;
    }

    @Override
    public void addCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener) {
        competitorSelectionChangeListeners.add(listener);
    }

    @Override
    public void removeCompetitorSelectionChangeListener(CompetitorSelectionChangeListener listener) {
        competitorSelectionChangeListeners.remove(listener);
    }

    private void fireCompetitorSelectionChanged(List<CompetitorDAO> selectedCompetitors) {
        for (CompetitorSelectionChangeListener listener : competitorSelectionChangeListeners) {
            listener.onCompetitorSelectionChange(selectedCompetitors);
        }
    }

    protected String toString(QuickRankDAO quickRank) {
        return quickRank.rank + ". " + quickRank.competitor.name + " ("
                + quickRank.competitor.threeLetterIocCountryCode + ") in leg #" + (quickRank.legNumber + 1);
    }

    public ListBox getListBox() {
        return quickRankListBox;
    }

}
