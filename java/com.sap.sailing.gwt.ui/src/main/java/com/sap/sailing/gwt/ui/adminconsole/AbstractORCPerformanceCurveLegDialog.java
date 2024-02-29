package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DoubleBox;

public abstract class AbstractORCPerformanceCurveLegDialog<T> extends DataEntryDialog<T> {
    private final StringMessages stringMessages;

    public AbstractORCPerformanceCurveLegDialog(String title, String message, StringMessages stringMessages,
            Validator<T> validator, DialogCallback<T> callback) {
        super(title, message, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
    }

    protected ListBox createLegTypeBox(ORCPerformanceCurveLeg orcLegParametersSoFar) {
        final ListBox legTypeBox = createListBox(/* isMultipleSelect */ false);
        legTypeBox.addItem("", (String) null);
        int i=1;
        for (final ORCPerformanceCurveLegTypes t : ORCPerformanceCurveLegTypes.values()) {
            legTypeBox.addItem(t.name(), t.name());
            if (orcLegParametersSoFar != null && orcLegParametersSoFar.getType() == t) {
                legTypeBox.setSelectedIndex(i);
            }
            final SelectElement selectElement = legTypeBox.getElement().cast();
            final NodeList<OptionElement> options = selectElement.getOptions();
            options.getItem(options.getLength()-1).setTitle(ORCPerformanceCurveLegTypeFormatter.getDescription(t, stringMessages));
            i++;
        }
        return legTypeBox;
    }

    protected void updateTwaBoxFromTwdAndLegDirection(DoubleBox twdBox, DoubleBox legDirectionBox, DoubleBox twaBox) {
        if (twdBox.getValue() != null && legDirectionBox.getValue() != null) {
            double twa = twdBox.getValue() - legDirectionBox.getValue();
            if (twa < -180) {
                twa += 360;
            } else if (twa > 180) {
                twa -= 360;
            }
            twaBox.setValue(twa);
        } else {
            twaBox.setText("");
        }
        validateAndUpdate();
    }
}
