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
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public class ResultSelectionAndApplyDialog extends DataEntryDialog<Triple<String, String, Pair<String, Date>>> {
    private final StringMessages stringMessages;
    private final LinkedHashMap<String, Triple<String, String, Pair<String, Date>>> values;

    public ResultSelectionAndApplyDialog(
            LeaderboardDTO leaderboard,
            SailingServiceAsync sailingService,
            StringMessages stringMessages,
            List<Triple<String, String, Pair<String, Date>>> values) {
        super(stringMessages.selectResultListToImportFrom(), stringMessages.selectResultListToImportFrom(),
                stringMessages.ok(), stringMessages.cancel(), new Validator(), new Callback(sailingService, leaderboard));
        this.stringMessages = stringMessages;
        this.values = new LinkedHashMap<String, Triple<String, String, Pair<String, Date>>>();
        for (Triple<String, String, Pair<String, Date>> v : values) {
            this.values.put("" + v.getA() + ": " + v.getB() + " - " + v.getC().getA() + " "
                    + stringMessages.of() + " " + v.getC().getB(), v);
        }
    }

    private static class Validator implements DataEntryDialog.Validator<Triple<String, String, Pair<String, Date>>> {
        @Override
        public String getErrorMessage(Triple<String, String, Pair<String, Date>> valueToValidate) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    private static class Callback implements AsyncCallback<Triple<String, String, Pair<String, Date>>> {
        private final LeaderboardDTO leaderboard;
        private final SailingServiceAsync sailingService;
        
        public Callback(SailingServiceAsync sailingService, LeaderboardDTO leaderboard) {
            this.sailingService = sailingService;
            this.leaderboard = leaderboard;
        }

        @Override
        public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSuccess(Triple<String, String, Pair<String, Date>> result) {
            // TODO Auto-generated method stub
        }
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        ListBox listBox = createListBox(/* isMultipleSelect */ false);
        for (String value : values.keySet()) {
            listBox.addItem(value);
        }
        return listBox;
    }


    @Override
    protected Triple<String, String, Pair<String, Date>> getResult() {
        // TODO Auto-generated method stub
        return null;
    }

}
