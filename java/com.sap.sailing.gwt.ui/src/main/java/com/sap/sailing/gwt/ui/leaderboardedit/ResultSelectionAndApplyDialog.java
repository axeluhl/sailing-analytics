package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.panels.BusyIndicator;
import com.sap.sailing.gwt.ui.client.shared.panels.SimpleBusyIndicator;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sse.gwt.ui.DataEntryDialog;

public class ResultSelectionAndApplyDialog extends DataEntryDialog<Triple<String, String, Pair<String, Date>>> {
    /**
     * a unique and human-readable string key for a eventNameBoatClassNameAndLastModfied pair
     */
    private final LinkedHashMap<String, Pair<String, Pair<String, Date>>> scoreCorrections;
    private final ListBox scoreCorrectionListBox;
    private final ListBox scoreCorrectionProviderListBox;
    private final BusyIndicator busyIndicator;
    
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final ErrorReporter errorReporter;

    private final Set<BoatClassDTO> boatClasses;
    
    public ResultSelectionAndApplyDialog(EditableLeaderboardPanel leaderboardPanel, Iterable<String> scoreCorrectionProviderNames, 
            SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter) {
        super(stringMessages.importOfficialResults(), null, stringMessages.ok(), stringMessages.cancel(), new Validator(),
                new Callback(sailingService, leaderboardPanel, errorReporter, stringMessages));
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;

        boatClasses = leaderboardPanel.getLeaderboard().getBoatClasses();

        this.scoreCorrections = new LinkedHashMap<String, Pair<String, Pair<String, Date>>>();

        scoreCorrectionProviderListBox = createListBox(/* isMultipleSelect */ false);
        scoreCorrectionListBox = createListBox(/* isMultipleSelect */ false);
        scoreCorrectionListBox.setVisible(false);
        busyIndicator = new SimpleBusyIndicator();
        
        scoreCorrectionProviderListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = scoreCorrectionProviderListBox.getSelectedIndex();
                if (selectedIndex > 0) {
                    String selectedProviderName = scoreCorrectionProviderListBox.getItemText(selectedIndex);
                    scoreCorrectionProviderChanged(selectedProviderName);
                } else {
                    scoreCorrectionProviderChanged(null);
                }
            }
        });
        
        List<String> sortedProviderNames = new ArrayList<String>();
        for(String providerName: scoreCorrectionProviderNames) {
            sortedProviderNames.add(providerName);
        }
        Collections.sort(sortedProviderNames);
        scoreCorrectionProviderListBox.addItem("Please select a result import provider...");
        for(String providerName: sortedProviderNames) {
            scoreCorrectionProviderListBox.addItem(providerName);
        }
    }

    private void scoreCorrectionProviderChanged(String selectedProviderName) {
        if (selectedProviderName != null) {
            busyIndicator.setBusy(true);
            scoreCorrectionProviderListBox.setEnabled(false);
            sailingService.getScoreCorrectionsOfProvider(selectedProviderName, new AsyncCallback<ScoreCorrectionProviderDTO>() {
                @Override
                public void onSuccess(ScoreCorrectionProviderDTO result) {
                    updateScoreCorrections(result);
                    scoreCorrectionListBox.setVisible(true);
                    busyIndicator.setBusy(false);
                    scoreCorrectionProviderListBox.setEnabled(true);
                }

                @Override
                public void onFailure(Throwable caught) {
                    updateScoreCorrections(null);
                    scoreCorrectionListBox.setVisible(false);
                    busyIndicator.setBusy(false);
                    scoreCorrectionProviderListBox.setEnabled(true);
                    errorReporter.reportError(stringMessages.errorLoadingScoreCorrectionProviders(caught.getMessage()));
                }
            });
        } else {
            scoreCorrectionListBox.setVisible(false);
            updateScoreCorrections(null);
        }
    }
    
    private void updateScoreCorrections(ScoreCorrectionProviderDTO scp) {
        scoreCorrections.clear();
        scoreCorrectionListBox.clear();

        if(scp != null) {
            scoreCorrectionListBox.addItem("Please select a scoring result...");
            
            List<Pair<String, Pair<String, Date>>> eventNameBoatClassNameAndLastModified = new ArrayList<Pair<String, Pair<String, Date>>>();
            for (Entry<String, Set<Pair<String, Date>>> entry : scp.getHasResultsForBoatClassFromDateByEventName().entrySet()) {
                for (Pair<String, Date> se : entry.getValue()) {
                    eventNameBoatClassNameAndLastModified.add(new Pair<String, Pair<String, Date>>(entry.getKey(), se));
                }
            }
            sortOfficialResultsByRelevance(eventNameBoatClassNameAndLastModified);
            
            for (Pair<String, Pair<String, Date>> pair : eventNameBoatClassNameAndLastModified) {
                String eventName = pair.getA();
                Pair<String, Date> boatClassAndLastModified = pair.getB();
                
                String scoreCorrectionName = eventName + ", " + boatClassAndLastModified.getA() + ", " + boatClassAndLastModified.getB(); 
                scoreCorrections.put(scoreCorrectionName, pair);
                scoreCorrectionListBox.addItem(scoreCorrectionName);
            }
        }
    }

    private void sortOfficialResultsByRelevance(List<Pair<String, Pair<String, Date>>> eventNameBoatClassNameCapturedWhen) {
        final Set<String> lowercaseBoatClassNames = new HashSet<String>();
        for (BoatClassDTO boatClass : boatClasses) {
            lowercaseBoatClassNames.add(boatClass.getName().toLowerCase());
        }
        Collections.sort(eventNameBoatClassNameCapturedWhen,
                new Comparator<Pair<String, Pair<String, Date>>>() {
                    @Override
                    public int compare(Pair<String, Pair<String, Date>> o1, Pair<String, Pair<String, Date>> o2) {
                        int result;
                        // TODO consider looking for longest common substring to handle things like "470 M" vs.
                        // "470 Men"
                        if (lowercaseBoatClassNames.contains(o1.getB().getA().toLowerCase())) {
                            if (lowercaseBoatClassNames.contains(o2.getB().getA().toLowerCase())) {
                                // both don't seem to have the right boat class; compare by time stamp; newest first
                                result = o2.getB().getB().compareTo(o1.getB().getB());
                            } else {
                                result = -1; // o1 scores "better", comes first, because it has the right boat class name
                            }
                        } else if (o2.getB().getA() != null
                                && lowercaseBoatClassNames.contains(o2.getB().getA().toLowerCase())) {
                            result = 1;
                        } else {
                            // both don't seem to have the right boat class; compare by time stamp; newest first
                            result = o2.getB().getB().compareTo(o1.getB().getB());
                        }
                        return result;
                    }
                });
    }

    private static class Validator implements DataEntryDialog.Validator<Triple<String, String, Pair<String, Date>>> {
        @Override
        public String getErrorMessage(Triple<String, String, Pair<String, Date>> valueToValidate) {
            String errorMessage = null;
            if(valueToValidate == null) {
                errorMessage = "";
            }
            return errorMessage;
        }
    }
    
    private static class Callback implements DialogCallback<Triple<String, String, Pair<String, Date>>> {
        private final EditableLeaderboardPanel leaderboardPanel;
        private final SailingServiceAsync sailingService;
        private final StringMessages stringMessages;
        private final ErrorReporter errorReporter;
        
        public Callback(SailingServiceAsync sailingService, EditableLeaderboardPanel leaderboardPanel, ErrorReporter errorReporter,
                StringMessages stringMessages) {
            this.sailingService = sailingService;
            this.leaderboardPanel = leaderboardPanel;
            this.stringMessages = stringMessages;
            this.errorReporter = errorReporter;
        }

        @Override
        public void cancel() {
            // don't do anything; dialog was canceled
        }

        @Override
        public void ok(Triple<String, String, Pair<String, Date>> providerNameAndEventNameBoatClassNameCapturedWhen) {
            final String scoreCorrectionProviderName = providerNameAndEventNameBoatClassNameCapturedWhen.getA();
            final String eventName = providerNameAndEventNameBoatClassNameCapturedWhen.getB();
            final String boatClassName = providerNameAndEventNameBoatClassNameCapturedWhen.getC().getA();
            final Date timePointWhenResultPublished = providerNameAndEventNameBoatClassNameCapturedWhen.getC().getB();
            leaderboardPanel.getBusyIndicator().setBusy(true);
            sailingService.getScoreCorrections(scoreCorrectionProviderName, eventName, boatClassName, timePointWhenResultPublished,
                    new AsyncCallback<RegattaScoreCorrectionDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            leaderboardPanel.getBusyIndicator().setBusy(false);
                            errorReporter.reportError(stringMessages.errorObtainingScoreCorrections(scoreCorrectionProviderName,
                                    eventName, boatClassName, timePointWhenResultPublished.toString(), caught.getMessage()));
                        }

                        @Override
                        public void onSuccess(RegattaScoreCorrectionDTO result) {
                            leaderboardPanel.getBusyIndicator().setBusy(false);
                            new MatchAndApplyScoreCorrectionsDialog(leaderboardPanel, stringMessages, sailingService,
                                    errorReporter, result).show();
                        }
            });
        }
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(busyIndicator);
        vPanel.add(scoreCorrectionProviderListBox);
        vPanel.add(scoreCorrectionListBox);
        return vPanel;
    }

    @Override
    public void show() {
        super.show();
        scoreCorrectionProviderListBox.setFocus(true);
    }

     @Override
    protected Triple<String, String, Pair<String, Date>> getResult() {
         Triple<String, String, Pair<String, Date>> result = null; 

         int selectedProviderIndex = scoreCorrectionProviderListBox.getSelectedIndex();
         if (selectedProviderIndex > 0) {
             String selectedProviderName = scoreCorrectionProviderListBox.getItemText(selectedProviderIndex);
             int selectedScoreCorrectionIndex = scoreCorrectionListBox.getSelectedIndex();
             if(selectedScoreCorrectionIndex > 0) {
                 Pair<String, Pair<String, Date>> pair = scoreCorrections.get(scoreCorrectionListBox.getValue(selectedScoreCorrectionIndex));
                 result = new Triple<String, String, Pair<String, Date>>(selectedProviderName, pair.getA(), pair.getB());
             }
         }
         return result;
    }
}
