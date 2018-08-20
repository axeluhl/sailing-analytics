package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * For a result discarding rule based on thresholds that tell after how many races the next discard kicks in, an instance of
 * this class offers the UI components and validation rules that help in composing a UI that, among other things, allows a user
 * to configure the discarding thresholds.<p>
 * 
 * TODO produce error messages during validation
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DiscardThresholdBoxes {
    private static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 15;

    private static final int NUMBER_OF_BOXES_PER_LINE = 5;

    private final LongBox[] discardThresholdBoxes;
    
    /**
     * The widget used to represent the UI
     */
    private final Widget widget;

    public DiscardThresholdBoxes(DataEntryDialog<?> parent, StringMessages stringMessages) {
        this(parent, /* values to show */ new int[0], stringMessages);
    }
    
    public DiscardThresholdBoxes(DataEntryDialog<?> parent, int[] initialDiscardThresholds, StringMessages stringMessages) {
        discardThresholdBoxes = new LongBox[MAX_NUMBER_OF_DISCARDED_RESULTS];
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            if (initialDiscardThresholds != null && i < initialDiscardThresholds.length) {
                discardThresholdBoxes[i] = parent.createLongBox(initialDiscardThresholds[i], 2);
            } else {
                discardThresholdBoxes[i] = parent.createLongBoxWithOptionalValue(null, 2);
            }
            discardThresholdBoxes[i].setVisibleLength(2);
        }
        widget = createDiscardThresholdBoxesPanel(stringMessages);
    }
    
    /**
     * @return the widget that can be added to the parent dialog passed to the constructor to visualize and edit the
     *         result discarding thresholds. The result returned from this object remains the same across this object's
     *         life time.
     */
    public Widget getWidget() {
        return widget;
    }
    
    public int[] getDiscardThresholds() {
        List<Integer> discardThresholds = new ArrayList<Integer>();
        // go backwards; starting from first non-zero element, add them; take over leading zeroes which validator shall discard
        for (int i = discardThresholdBoxes.length-1; i>=0; i--) {
            if ((discardThresholdBoxes[i].getValue() != null
                    && discardThresholdBoxes[i].getValue().toString().length() > 0) || !discardThresholds.isEmpty()) {
                if (discardThresholdBoxes[i].getValue() == null) {
                    discardThresholds.add(0, 0);
                } else {
                    discardThresholds.add(0, discardThresholdBoxes[i].getValue().intValue());
                }
            }
        }
        int[] discardThresholdsBoxContents = new int[discardThresholds.size()];
        for (int i = 0; i < discardThresholds.size(); i++) {
            discardThresholdsBoxContents[i] = discardThresholds.get(i);
        }
        return discardThresholdsBoxContents;
    }

    private Widget createDiscardThresholdBoxesPanel(StringMessages stringMessages) {
        assert discardThresholdBoxes != null && discardThresholdBoxes.length == MAX_NUMBER_OF_DISCARDED_RESULTS;
        final VerticalPanel vp = new VerticalPanel();
        vp.add(new Label(stringMessages.discardRacesFromHowManyStartedRacesOn()));
        final Grid grid = new Grid(0, 2*NUMBER_OF_BOXES_PER_LINE);
        grid.setCellSpacing(3);
        vp.add(grid);
        for (int i = 0; i < discardThresholdBoxes.length; i++) {
            if (i%NUMBER_OF_BOXES_PER_LINE == 0) {
                grid.resizeRows(i/NUMBER_OF_BOXES_PER_LINE + 1);
            }
            grid.setWidget(i/NUMBER_OF_BOXES_PER_LINE, 2*(i%NUMBER_OF_BOXES_PER_LINE), new Label("" + (i + 1) + "."));
            grid.setWidget(i/NUMBER_OF_BOXES_PER_LINE, 2*(i%NUMBER_OF_BOXES_PER_LINE)+1, discardThresholdBoxes[i]);
        }
        return vp;
    }
    
    public static String getErrorMessage(int[] discardThresholds, StringMessages stringMessages) {
        String errorMessage = null;
        if (discardThresholds != null) {
            boolean discardThresholdsAscending = true;
            for (int i = 1; i < discardThresholds.length; i++) {
                if (0 < discardThresholds.length) {
                    discardThresholdsAscending = discardThresholdsAscending
                            && discardThresholds[i - 1] < discardThresholds[i]
                            // and if one box is empty, all subsequent boxes need to be empty too
                            && (discardThresholds[i] == 0 || discardThresholds[i - 1] > 0);
                }
            }
            if (!discardThresholdsAscending) {
                errorMessage = stringMessages.discardThresholdsMustBeAscending();
            }
        }
        return errorMessage;
    }
}
