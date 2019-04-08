package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.Arrays;

import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class EditScoreDialog extends DataEntryDialog<Util.Pair<MaxPointsReason, Double>> {
    private final ListBox maxPointsBox;
    private final DoubleBox totalPointsBox;
    private final StringMessages stringMessages;
    
    public EditScoreDialog(StringMessages stringMessages, String competitorName, String raceColumnName,
            MaxPointsReason oldMaxPointsReason, Double oldTotalPoints, DialogCallback<Util.Pair<MaxPointsReason, Double>> callback) {
        super(stringMessages.correctScore(), stringMessages.correctScoreFor(competitorName, raceColumnName),
                stringMessages.ok(), stringMessages.cancel(), /* validator */ null, /* animationEnabled */ true,
                callback);
        this.stringMessages = stringMessages;
        maxPointsBox = createListBox(/* isMultipleSelect */ false);
        maxPointsBox.addItem("");
        for (MaxPointsReason maxPointsReason : MaxPointsReason.values()) {
            maxPointsBox.addItem(maxPointsReason.name());
        }
        if (oldMaxPointsReason == null) {
            maxPointsBox.setSelectedIndex(0);
        } else {
            maxPointsBox.setSelectedIndex(1+Arrays.asList(MaxPointsReason.values()).indexOf(oldMaxPointsReason));
        }
        totalPointsBox = createDoubleBox(/* visibleLength */ 5);
        if (oldTotalPoints != null) {
            totalPointsBox.setValue(oldTotalPoints);
        }
    }

    @Override
    protected Util.Pair<MaxPointsReason, Double> getResult() {
        final MaxPointsReason maxPointsReason;
        if ("".equals(maxPointsBox.getItemText(maxPointsBox.getSelectedIndex()))) {
            maxPointsReason = null;
        } else {
            maxPointsReason = MaxPointsReason.valueOf(maxPointsBox.getItemText(maxPointsBox.getSelectedIndex()));
        }
        final Double totalScore = totalPointsBox.getValue();
        return new Util.Pair<MaxPointsReason, Double>(maxPointsReason, totalScore);
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, new Label(stringMessages.penaltyOrRedress()));
        grid.setWidget(0, 1, maxPointsBox);
        grid.setWidget(1, 0, new Label(stringMessages.totalScore()));
        grid.setWidget(1, 1, totalPointsBox);
        return grid;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return totalPointsBox;
    }
    
    @Override
    public void show() {
        super.show();
        totalPointsBox.selectAll();
    }
}
