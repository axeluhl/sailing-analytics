package com.sap.sse.datamining.ui.client.selection.statistic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;

public class AggregatorListBox extends ValueListBox<AggregatorGroup> {
    
    private static final String UnsupportedItemStyle = "unsupportedAggregatorListItem";
    
    private final Map<String, AggregatorGroup> aggregatorsByDisplayName;
    
    public AggregatorListBox(String nullDisplayString) {
        super(new AbstractRenderer<AggregatorGroup>() {
            @Override
            public String render(AggregatorGroup aggregator) {
                return aggregator == null ? nullDisplayString : aggregator.getDisplayName();
            }
        });
        aggregatorsByDisplayName = new HashMap<>();
    }
    
    public void updateItemStyles(ExtractionFunctionWithContext extractionFunction) {
        ListBox listBox = (ListBox) getWidget();
        SelectElement selectElement = SelectElement.as(listBox.getElement());
        NodeList<OptionElement> options = selectElement.getOptions();
        for (int i = 0; i < options.getLength(); i++) {
            OptionElement option = options.getItem(i);
            AggregatorGroup aggregator = aggregatorsByDisplayName.get(option.getText());
            String className = aggregator == null || extractionFunction == null
                    || aggregator.supportsFunction(extractionFunction) ? "" : UnsupportedItemStyle;
            option.setClassName(className);
        }
    }
    
    @Override
    public void setAcceptableValues(Collection<AggregatorGroup> newValues) {
        aggregatorsByDisplayName.clear();
        for (AggregatorGroup aggregator : newValues) {
            aggregatorsByDisplayName.put(aggregator.getDisplayName(), aggregator);
        }
        super.setAcceptableValues(newValues);
    }
    
}