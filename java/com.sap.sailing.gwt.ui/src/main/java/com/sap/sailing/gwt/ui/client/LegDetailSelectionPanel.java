package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.LegDetailSelectionProvider.LegDetailColumnType;

public class LegDetailSelectionPanel extends DataEntryDialog<List<LegDetailColumnType>> {
    private final LegDetailSelectionProvider selectionProvider;
    private final Map<LegDetailColumnType, CheckBox> checkboxes;
    
    public LegDetailSelectionPanel(LegDetailSelectionProvider selectionProvider, String title,
            String message, String okButtonName, String cancelButtonName,
            com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator<List<LegDetailColumnType>> validator,
            AsyncCallback<List<LegDetailColumnType>> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        this.selectionProvider = selectionProvider;
        checkboxes = new HashMap<LegDetailSelectionProvider.LegDetailColumnType, CheckBox>();
    }

    @Override
    protected Widget getAdditionalWidget() {
        List<LegDetailColumnType> currentSelection = selectionProvider.getLegDetailsToShow();
        VerticalPanel vp = new VerticalPanel();
        for (LegDetailColumnType type : LegDetailColumnType.values()) {
            CheckBox checkbox = new CheckBox(type.toString(), currentSelection.contains(type));
            checkboxes.put(type, checkbox);
            vp.add(checkbox);
        }
        return vp;
    }

    @Override
    protected List<LegDetailColumnType> getResult() {
        List<LegDetailColumnType> result = new ArrayList<LegDetailSelectionProvider.LegDetailColumnType>();
        for (Map.Entry<LegDetailColumnType, CheckBox> entry : checkboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }
}
