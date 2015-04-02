package com.sap.sailing.gwt.ui.datamining.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        
        add(filterPanel);
        add(table);
    }
    
    private String getElementAsString(ContentType element) {
        return element.toString();
    }
    
    /**
     * Replaces the current content of the table with the new one, while preserving the current selection.<br />
     * Returns <code>true</code>, if:
     * <ul>
     *   <li>Previously selected elements have been removed</li>
     *   <li>Nothing was selected and elements have been added or removed</li>
     * </ul>
     * 
     * @param newContent The new content.
     * @return <code>true</code>, if query related elements has been changed.
     */
    @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
    public boolean updateContent(Collection<?> newContent) {
        Collection<ContentType> specificNewContent = null;
        try {
            specificNewContent = (Collection<ContentType>) newContent;
        } catch (ClassCastException e) {
            return false;
        }
        
        Collection<ContentType> oldContent = new ArrayList<ContentType>(dataProvider.getList());
        Collection<ContentType> selection = selectionModel.getSelectedSet();
        
        dataProvider.getList().clear();
        dataProvider.getList().addAll(specificNewContent);
        allData.clear();
        allData.addAll(specificNewContent);
        filterPanel.updateAll(allData);
        
        return selection.isEmpty() ? elementsHaveBeenAddedOrRemoved(oldContent, specificNewContent) : selectedElementsHaveBeenRemoved(selection, specificNewContent);
    }
    
    private boolean selectedElementsHaveBeenRemoved(Collection<ContentType> selection, Collection<ContentType> newContent) {
        Set<Object> selectionKeys = getKeysFor(selection);
        Set<Object> newContentKeys = getKeysFor(newContent);
        return !newContentKeys.containsAll(selectionKeys);
    }

    private boolean elementsHaveBeenAddedOrRemoved(Collection<ContentType> oldContent, Collection<ContentType> newContent) {
        if (oldContent.size() != newContent.size()) {
            return true;
        }
        
        Set<Object> oldContentKeys = getKeysFor(oldContent);
        Set<Object> newContentKeys = getKeysFor(newContent);
        return !newContentKeys.containsAll(oldContentKeys);
    }

    private Set<Object> getKeysFor(Collection<ContentType> content) {
        Set<Object> contentKeys = new HashSet<Object>();
        for (ContentType element : content) {
            contentKeys.add(dataProvider.getKey(element));
        }
        return contentKeys;
    }

    public Collection<ContentType> getSelection() {
        return selectionModel.getSelectedSet();
    }

    public void setSelection(Iterable<?> elements) {
        clearSelection();
        try {
            @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
            Iterable<ContentType> specificContent = (Iterable<ContentType>) elements;
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
    
    public boolean isFilterWidgetVisible() {
        return filterPanel.isVisible();
    }
    
    public void setFilterWidgetVisible(boolean visible) {
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
        
        int tableHeight = isFilterWidgetVisible() ? Math.max(0, height - filterPanel.getOffsetHeight()) : height;
        table.setSize(width + "px", tableHeight + "px");
        //FIXME Force a rerendering of the table. Switching the retriever level fixes the display
        table.redraw();
    }


}
