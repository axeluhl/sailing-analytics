package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.datamining.shared.SharedDimension;

public abstract class SelectionTable<ContentType, ValueType> extends SimplePanel implements Comparator<ContentType> {
    
    private SharedDimension dimension;
    
    private DataGrid<ContentType> table;
    private MultiSelectionModel<ContentType> selectionModel;
    private ListDataProvider<ContentType> dataProvider;
    
    public SelectionTable(String title, SharedDimension dimension) {
        this.dimension = dimension;
        
        table = new DataGrid<ContentType>();
        table.setAutoHeaderRefreshDisabled(true);
        table.setAutoFooterRefreshDisabled(true);
        
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
    
    public Collection<ContentType> getContent() {
        return dataProvider.getList();
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
    
    @Override
    public int compare(ContentType content1, ContentType content2) {
        return getValueAsString(content1).compareTo(getValueAsString(content2));
    }

    public abstract ValueType getValue(ContentType content);
    
    public String getValueAsString(ContentType content) {
        return getValue(content).toString();
    }
    
    public void addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        selectionModel.addSelectionChangeHandler(handler);
    }
    
    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        table.setWidth(width);
    }
    
    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        table.setHeight(height);
    }

}
