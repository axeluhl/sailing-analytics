package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.datamining.shared.SharedDimension;

public abstract class SelectionTable<ContentType, ValueType> extends SimplePanel {
    
    private SharedDimension dimension;
    
    private CellTable<ContentType> table;
    private MultiSelectionModel<ContentType> selectionModel;
    private ListDataProvider<ContentType> dataProvider;
    
    public SelectionTable(String title, SharedDimension dimension) {
        this.dimension = dimension;
        
        table = new CellTable<ContentType>(500);
        table.addColumn(new TextColumn<ContentType>() {
            @Override
            public String getValue(ContentType content) {
                return SelectionTable.this.getValueAsString(content);
            }
        }, title);
        selectionModel = new MultiSelectionModel<ContentType>();
        table.setSelectionModel(selectionModel);
        
        dataProvider = new ListDataProvider<ContentType>();
        dataProvider.addDataDisplay(table);
        
        setWidget(table);
    }

    public SharedDimension getDimension() {
        return dimension;
    }
    
    public void setContent(Collection<ContentType> content) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(content);
    }
    
    public Collection<?> getSelection() {
        Collection<ValueType> selectionAsValues = new HashSet<ValueType>();
        for (ContentType content : selectionModel.getSelectedSet()) {
            selectionAsValues.add(getValue(content));
        }
        return selectionAsValues;
    }

    public void setSelection(Iterable<?> elements) {
        clearSelection();
        try {
            @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
            Iterable<ContentType> elementsMatchingContent = (Iterable<ContentType>) elements;
            for (ContentType element : elementsMatchingContent) {
                selectionModel.setSelected(element, true);
            }
        } catch (ClassCastException e) {/*Ignore the elements, because they don't fit the ContentType*/}
    }
    
    public void clearSelection() {
        selectionModel.clear();
    }

    public abstract ValueType getValue(ContentType content);
    
    public String getValueAsString(ContentType content) {
        return getValue(content).toString();
    }
    
    public void addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        selectionModel.addSelectionChangeHandler(handler);
    }

}
