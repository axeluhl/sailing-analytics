package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.HashSet;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;

public abstract class SelectionTable<DimensionType, ContentType, ValueType> extends SimplePanel {
    
    private DimensionType dimension;
    
    private CellTable<ContentType> table;
    private MultiSelectionModel<ContentType> selectionModel;
    private ListDataProvider<ContentType> dataProvider;
    
    public SelectionTable(String title, DimensionType dimension) {
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

    public DimensionType getDimension() {
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
