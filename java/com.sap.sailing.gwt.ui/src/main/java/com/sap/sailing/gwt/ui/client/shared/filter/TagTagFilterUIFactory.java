package com.sap.sailing.gwt.ui.client.shared.filter;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sse.common.filter.TextOperator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class TagTagFilterUIFactory extends AbstractTagTextFilterUIFactory {
    private TextBox valueTextBox;
    private ListBox operatorSelectionListBox;

    public TagTagFilterUIFactory() {
        this(new TagTagFilter());
    }

    public TagTagFilterUIFactory(TagTagFilter tagTagFilter) {
        super(tagTagFilter, TextOperator.Operators.Equals);

        supportedOperators.add(TextOperator.Operators.Equals);
        supportedOperators.add(TextOperator.Operators.NotEqualTo);
        supportedOperators.add(TextOperator.Operators.Contains);
        supportedOperators.add(TextOperator.Operators.NotContains);
        supportedOperators.add(TextOperator.Operators.StartsWith);
        supportedOperators.add(TextOperator.Operators.EndsWith);

        valueTextBox = null;
        operatorSelectionListBox = null;
    }

    @Override
    public Widget createFilterUIWidget(DataEntryDialog<?> dataEntryDialog) {
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(createOperatorSelectionWidget(dataEntryDialog));
        hp.add(createValueInputWidget(dataEntryDialog));
        hp.setSpacing(5);
        return hp;
    }

    private Widget createValueInputWidget(DataEntryDialog<?> dataEntryDialog) {
        if (valueTextBox == null) {
            valueTextBox = dataEntryDialog.createTextBox(tagTextFilter.getValue());
            valueTextBox.setVisibleLength(20);
            valueTextBox.setFocus(true);
        }
        return valueTextBox;
    }

    private Widget createOperatorSelectionWidget(DataEntryDialog<?> dataEntryDialog) {
        if (operatorSelectionListBox == null) {
            operatorSelectionListBox = createOperatorSelectionListBox(dataEntryDialog);
        }
        return operatorSelectionListBox;
    }

    @Override
    public FilterWithUI<TagDTO> createFilterFromUI() {
        TagTagFilter result = null;
        if (valueTextBox != null && operatorSelectionListBox != null) {
            result = new TagTagFilter();
            TextOperator.Operators op = TextOperator.Operators
                    .valueOf(operatorSelectionListBox.getValue(operatorSelectionListBox.getSelectedIndex()));
            TextOperator textOperator = new TextOperator(op);
            result.setOperator(textOperator);
            result.setValue(valueTextBox.getValue());
        }
        return result;
    }
}