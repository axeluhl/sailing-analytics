package com.sap.sse.datamining.ui.client.selection.statistic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;

public class AggregatorListBox extends ValueListBox<AggregatorGroup> {
    
    @FunctionalInterface
    public static interface ValueChangeHandler {
        void valueChanged(AggregatorGroup oldValue, AggregatorGroup newValue);
    }
    
    private static final String UnsupportedItemStyle = "unsupportedAggregatorListItem";
    
    private final Map<String, AggregatorGroup> aggregatorsByDisplayName;
    private AggregatorGroup value;
    private ValueChangeHandler valueChangeHandler;
    
    public AggregatorListBox(String nullDisplayString) {
        super(new AbstractRenderer<AggregatorGroup>() {
            @Override
            public String render(AggregatorGroup aggregator) {
                return aggregator == null ? nullDisplayString : aggregator.getDisplayName();
            }
        });
        aggregatorsByDisplayName = new HashMap<>();
        
        addValueChangeHandler(event -> {
            AggregatorGroup newValue = event.getValue();
            if (!Objects.equals(value, newValue)) {
                AggregatorGroup oldValue = value;
                value = newValue;
                if (valueChangeHandler != null) {
                    valueChangeHandler.valueChanged(oldValue, value);
                }
            }
        });
    }
    
    public void setValueChangeHandler(ValueChangeHandler valueChangeHandler) {
        this.valueChangeHandler = valueChangeHandler;
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
        Collection<AggregatorGroup> newValuesWithNull = new ArrayList<>();
        newValuesWithNull.add(null);
        
        aggregatorsByDisplayName.clear();
        for (AggregatorGroup aggregator : newValues) {
            if (aggregator != null) {
                aggregatorsByDisplayName.put(aggregator.getDisplayName(), aggregator);
                newValuesWithNull.add(aggregator);
            }
        }
        
        super.setAcceptableValues(newValuesWithNull);
    }
    
}