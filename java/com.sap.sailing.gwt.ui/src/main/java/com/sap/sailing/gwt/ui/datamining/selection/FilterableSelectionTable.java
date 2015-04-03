package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;

public class FilterableSelectionTable<ContentType extends Serializable> extends FlowPanel {
    
    private final Collection<ContentType> allData;
    private int width;
    private int height;

    private final AbstractFilterablePanel<ContentType> filterPanel;
    private String filterText;
    private final DataGrid<ContentType> table;
    private final MultiSelectionModel<ContentType> selectionModel;
    private final ListDataProvider<ContentType> dataProvider;
    
    public FilterableSelectionTable() {
        allData = new ArrayList<ContentType>();
        
        table = new DataGrid<ContentType>();
        table.setAutoHeaderRefreshDisabled(true);
        table.setAutoFooterRefreshDisabled(true);
        
        TextColumn<ContentType> contentColumn = new TextColumn<ContentType>() {
            @Override
            public String getValue(ContentType content) {
                return FilterableSelectionTable.this.getElementAsString(content);
            }
        };
        table.addColumn(contentColumn);

        selectionModel = new MultiSelectionModel<ContentType>();
        table.setSelectionModel(selectionModel);
        
        dataProvider = new ListDataProvider<ContentType>(new ProvidesKey<ContentType>() {
            @Override
            public Object getKey(ContentType item) {
                return getElementAsString(item);
            }
        });
        dataProvider.addDataDisplay(table);
        
        filterPanel = new AbstractFilterablePanel<ContentType>(allData, table, dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(ContentType content) {
                Collection<String> searchableStrings = new ArrayList<String>();
                searchableStrings.add(getElementAsString(content));
                return searchableStrings;
            }
        };
        filterPanel.setSpacing(2);
        filterPanel.setWidth("95%");
        filterPanel.getTextBox().setWidth("95%");
        filterPanel.setVisible(false);
        filterText = "";
        
        add(filterPanel);
        add(table);
    }
    
    private String getElementAsString(ContentType element) {
        return element.toString();
    }
    
    @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
    public void setContent(Collection<?> newContent) {
        Collection<ContentType> specificNewContent = null;
        try {
            specificNewContent = (Collection<ContentType>) newContent;
        } catch (ClassCastException e) {
            return;
        }
        
        allData.clear();
        allData.addAll(specificNewContent);
        dataProvider.getList().clear();
        dataProvider.getList().addAll(allData);
        clearSelection();
        filterPanel.updateAll(allData);
    }

    public Collection<ContentType> getSelection() {
        return selectionModel.getSelectedSet();
    }

    public void setSelection(Iterable<?> elements) {
        try {
            @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
            Iterable<ContentType> specificContent = (Iterable<ContentType>) elements;
            clearSelection();
            for (ContentType element : specificContent) {
                selectionModel.setSelected(element, true);
            }
        } catch (ClassCastException e) {/*Ignore the elements, because they don't match the ContentType*/}
    }
    
    public void clearSelection() {
        selectionModel.clear();
    }
    
    public void addSelectionChangeHandler(SelectionChangeEvent.Handler handler) {
        selectionModel.addSelectionChangeHandler(handler);
    }
    
    public boolean isFilteringEnabled() {
        return filterPanel.isVisible();
    }
    
    public void setFilteringEnabled(boolean visible) {
        if (visible) {
            filterPanel.getTextBox().setText(filterText);
            filterPanel.filter();
        } else {
            filterText = filterPanel.getTextBox().getText();
            filterPanel.getTextBox().setText("");
            filterPanel.filter();
        }
        
        filterPanel.setVisible(visible);
        resizeTo(width, height);
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        //FIXME Force a rerendering of the table. Switching the retriever level fixes the display
        table.redraw();
    }

    public void resizeTo(int widthInPX, int heightInPX) {
        width = widthInPX;
        height = heightInPX;
        
        setSize(width + "px", height+ "px");
        filterPanel.setWidth(width + "px");
        int tableHeight = filterPanel.isVisible() ? Math.max(0, height - filterPanel.getOffsetHeight()) : height;
        table.setSize(width + "px", tableHeight + "px");
        //FIXME Force a rerendering of the table. Switching the retriever level fixes the display
        table.redraw();
    }


}
