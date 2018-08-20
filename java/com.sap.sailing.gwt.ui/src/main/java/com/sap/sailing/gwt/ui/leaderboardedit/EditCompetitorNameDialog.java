package com.sap.sailing.gwt.ui.leaderboardedit;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class EditCompetitorNameDialog extends DataEntryDialog<String> {
    private final TextBox competitorNameBox;
    private final StringMessages stringMessages;
    
    public EditCompetitorNameDialog(StringMessages stringMessages, String competitorName,
            DialogCallback<String> callback) {
        super(stringMessages.competitor(), stringMessages.competitor(),
                stringMessages.ok(), stringMessages.cancel(), /* validator */ null, /* animationEnabled */ true,
                callback);
        this.stringMessages = stringMessages;
        this.competitorNameBox = createTextBox(competitorName);
    }

    @Override
    protected String getResult() {
        return this.competitorNameBox.getValue();
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, new Label(stringMessages.competitor()));
        grid.setWidget(0, 1, competitorNameBox);
        return grid;
    }
}
