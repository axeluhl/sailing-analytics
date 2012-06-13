package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;

public class ResultSelectionAndApplyDialog extends DataEntryDialog<Triple<String, String, Pair<String, Date>>> {
    private final LinkedHashMap<String, Triple<String, String, Pair<String, Date>>> values;
    private ListBox listBox;

    public ResultSelectionAndApplyDialog(
            LeaderboardDTO leaderboard,
            SailingServiceAsync sailingService,
            StringMessages stringMessages,
            List<Triple<String, String, Pair<String, Date>>> values, ErrorReporter errorReporter) {
        super(stringMessages.selectResultListToImportFrom(), stringMessages.selectResultListToImportFrom(),
                stringMessages.ok(), stringMessages.cancel(), new Validator(),
                new Callback(sailingService, leaderboard, errorReporter, stringMessages));
        this.values = new LinkedHashMap<String, Triple<String, String, Pair<String, Date>>>();
        for (Triple<String, String, Pair<String, Date>> v : values) {
            this.values.put("" + v.getA() + ": " + v.getB() + " - " + v.getC().getA() + " "
                    + stringMessages.of() + " " + v.getC().getB(), v);
        }
        listBox = createListBox(/* isMultipleSelect */ false);
    }

    private static class Validator implements DataEntryDialog.Validator<Triple<String, String, Pair<String, Date>>> {
        @Override
        public String getErrorMessage(Triple<String, String, Pair<String, Date>> valueToValidate) {
            // nothing can go wrong here
            return null;
        }
    }
    
    private static class Callback implements AsyncCallback<Triple<String, String, Pair<String, Date>>> {
        private final LeaderboardDTO leaderboard;
        private final SailingServiceAsync sailingService;
        private final StringMessages stringMessages;
        private final ErrorReporter errorReporter;
        
        public Callback(SailingServiceAsync sailingService, LeaderboardDTO leaderboard, ErrorReporter errorReporter,
                StringMessages stringMessages) {
            this.sailingService = sailingService;
            this.leaderboard = leaderboard;
            this.stringMessages = stringMessages;
            this.errorReporter = errorReporter;
        }

        @Override
        public void onFailure(Throwable caught) {
            // don't do anything; dialog was canceled
        }

        @Override
        public void onSuccess(Triple<String, String, Pair<String, Date>> result) {
            final String scoreCorrectionProviderName = result.getA();
            final String eventName = result.getB();
            final String boatClassName = result.getC().getA();
            final Date timePointWhenResultPublished = result.getC().getB();
            sailingService.getScoreCorrections(scoreCorrectionProviderName, eventName, boatClassName, timePointWhenResultPublished,
                    new AsyncCallback<RegattaScoreCorrectionDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(stringMessages.errorObtainingScoreCorrections(scoreCorrectionProviderName,
                                    eventName, boatClassName, timePointWhenResultPublished.toString()));
                        }

                        @Override
                        public void onSuccess(RegattaScoreCorrectionDTO result) {
                            new MatchAndApplyScoreCorrectionsDialog(leaderboard, stringMessages, sailingService, errorReporter, result).show();
                        }
            });
        }
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        for (String value : values.keySet()) {
            listBox.addItem(value);
        }
        return listBox;
    }

    @Override
    public void show() {
        super.show();
        listBox.setFocus(true);
    }

    @Override
    protected Triple<String, String, Pair<String, Date>> getResult() {
        final int selectedIndex = listBox.getSelectedIndex();
        if (selectedIndex != -1) {
            return values.get(listBox.getValue(selectedIndex));
        } else {
            return null;
        }
    }

}
