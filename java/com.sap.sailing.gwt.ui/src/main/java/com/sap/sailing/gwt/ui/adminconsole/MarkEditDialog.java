package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.racemap.Pattern;
import com.sap.sailing.gwt.ui.shared.racemap.Shape;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class MarkEditDialog extends DataEntryDialog<MarkDTO> {
    private final MarkDTO markToEdit; //color, shape, pattern, type
    private final TextBox name;
    private final ListBox shape;
    private final ListBox pattern;
    private final ListBox type;
    private final TextBox color;
    private final StringMessages stringMessages;
    
    public MarkEditDialog(final StringMessages stringMessages, MarkDTO markToEdit, boolean isNewMark,
            DialogCallback<MarkDTO> callback) {
        super(isNewMark ? stringMessages.add(stringMessages.mark()) : stringMessages.edit(stringMessages.mark()),
                isNewMark ? stringMessages.add(stringMessages.mark()) : stringMessages.edit(stringMessages.mark()),
                        stringMessages.ok(), stringMessages.cancel(), new DataEntryDialog.Validator<MarkDTO>() {
                    @Override
                    public String getErrorMessage(MarkDTO valueToValidate) {
                        String result = null;
                        if (valueToValidate.getName() == null || valueToValidate.getName().isEmpty()) {
                            result = stringMessages.pleaseEnterAName();
                        } else if (valueToValidate.color != null && ! valueToValidate.color.isEmpty()) {
                            try {
                                new RGBColor(valueToValidate.color);
                            } catch (IllegalArgumentException iae) {
                                result = valueToValidate.color;
                            }
                        }
                        return result;
                    }
                }, /* animationEnabled */true, callback);
        this.stringMessages = stringMessages;
        this.markToEdit = markToEdit;

        this.name = createTextBox(markToEdit.getName());
        this.shape = createAndSelectListBox(Shape.values(), markToEdit.shape, true);
        this.type = createAndSelectListBox(MarkType.values(), markToEdit.type == null ? null : markToEdit.type.name(), false);
        this.color = createTextBox(markToEdit.color);
        this.pattern = createAndSelectListBox(Pattern.values(), markToEdit.pattern, true);
    }
    
    private <T extends Enum<T>> ListBox createAndSelectListBox(T[] values, String toSelect, boolean emptySelectionPossible) {
        ListBox result = createListBox(/* isMultipleSelect */ false);
        int i=0;
        List<String> list = new ArrayList<String>();
        for (T t : values) {
            list.add(t.name());
        }
        if (emptySelectionPossible) {
            list.add("");
        }
        Collections.sort(list);
        for (String name : list) {
            result.addItem(name);
            if (name.equals(toSelect)) {
                result.setSelectedIndex(i);
            }
            i++;
        }
        return result;
    }

    
    @Override
    public void show() {
        super.show();
        name.setFocus(true);
    }

    @Override
    protected MarkDTO getResult() {
        MarkDTO result = new MarkDTO(markToEdit.getIdAsString(), name.getText());
        result.shape = shape.getItemText(shape.getSelectedIndex());
        result.type = MarkType.valueOf(type.getItemText(type.getSelectedIndex()));
        result.color = color.getText();
        result.pattern = pattern.getItemText(pattern.getSelectedIndex());
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(5, 2);
        result.setWidget(0, 0, new Label(stringMessages.name()));
        result.setWidget(0, 1, name);
        result.setWidget(1, 0, new Label(stringMessages.shape()));
        result.setWidget(1, 1, shape);
        result.setWidget(2, 0, new Label(stringMessages.type()));
        result.setWidget(2, 1, type);
        result.setWidget(3, 0, new Label(stringMessages.color()));
        result.setWidget(3, 1, color);
        result.setWidget(4, 0, new Label(stringMessages.pattern()));
        result.setWidget(4, 1, pattern);
        return result;
    }

}
