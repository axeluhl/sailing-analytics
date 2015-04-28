package com.sap.sailing.gwt.ui.datamining.selection.filter;

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
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;

public class FilterableSelectionTable<ContentType extends Serializable> extends FlowPanel {
    
    private final Set<FilterSelectionChangedListener> listeners;
    private boolean blockSelectionChangeNotification;
    
    private final Collection<ContentType> allData;
    private int width;
    private int height;

    private final AbstractFilterablePanel<ContentType> filterPanel;
    private String filterText;
    private final DataGrid<ContentType> table;
    private final MultiSelectionModel<ContentType> selectionModel;
    private final ListDataProvider<ContentType> dataProvider;
    
    public FilterableSelectionTable() {
        listeners = new HashSet<>();
        
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
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (!blockSelectionChangeNotification) {
                    notifyListeners();
                }
            }
        });
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

    /**
     * Replaces the current content of the table with the new one and clears the current selection.<br>
     * Returns <code>true</code> and notifies the listeners, if the selection changed.
     * @param newContent
     */
    @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
    public boolean setContent(Collection<?> newContent, boolean notifyListenersWhenSelectionChanged) {
        Collection<ContentType> specificNewContent = null;
        try {
            specificNewContent = (Collection<ContentType>) newContent;
        } catch (ClassCastException e) {
            return false;
        }
        internalSetContent(specificNewContent);
        Set<ContentType> previousSelection = selectionModel.getSelectedSet();
        if (notifyListenersWhenSelectionChanged) {
            clearSelection();
        } else {
            blockSelectionChangeNotification = true;
            clearSelection();
            blockSelectionChangeNotification = false;
        }
        return !previousSelection.isEmpty();
    }
    
    /**
     * Replaces the current content of the table with the new one, while preserving the current selection.<br>
     * Returns <code>true</code> and notifies the listeners, if previously selected elements have been removed.
     * @param newContent
     * @return <code>true</code>, if previously selected elements have been removed.
     */
    @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
    public boolean updateContent(Collection<?> newContent, boolean notifyListenersWhenSelectionChanged) {
        Collection<ContentType> specificNewContent = null;
        try {
            specificNewContent = (Collection<ContentType>) newContent;
        } catch (ClassCastException e) {
            return false;
        }
        internalSetContent(specificNewContent);
        return cleanSelection(notifyListenersWhenSelectionChanged);
    }

    private void internalSetContent(Collection<ContentType> specificNewContent) {
        allData.clear();
        allData.addAll(specificNewContent);
        dataProvider.getList().clear();
        dataProvider.getList().addAll(allData);
        filterPanel.updateAll(allData);
    }

    /**
     * Unselects the selected elements, that aren't in the data anymore.
     * @return <code>true</code>, if elements have been unselected;
     */
    private boolean cleanSelection(boolean notifyListenersWhenSelectionChanged) {
        boolean selectionChanged = false;
        blockSelectionChangeNotification = true;
        
        for (ContentType selectedElement : getSelection()) {
            Object selectedElementKey = dataProvider.getKey(selectedElement);
            boolean contentContainsSelectedElement = false;
            for (ContentType contentElement : allData) {
                if (selectedElementKey.equals(dataProvider.getKey(contentElement))) {
                    contentContainsSelectedElement = true;
                    break;
                }
            }
            if (!contentContainsSelectedElement) {
                selectionModel.setSelected(selectedElement, false);
                selectionChanged = true;
            }
        }
        
        blockSelectionChangeNotification = false;
        if (selectionChanged && notifyListenersWhenSelectionChanged) {
            notifyListeners();
        }
        return selectionChanged;
    }

    private void notifyListeners() {
        for (FilterSelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
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
    
    public void addSelectionChangeHandler(FilterSelectionChangedListener handler) {
        listeners.add(handler);
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
