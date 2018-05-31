package com.sap.sailing.gwt.ui.datamining.selection.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;

public class FilterableSelectionTable<ContentType extends Serializable> {

    private final Set<FilterSelectionChangedListener> listeners;
    
    private final Collection<ContentType> allData;

    private final FlowPanel mainPanel;
    private final AbstractFilterablePanel<ContentType> filterPanel;
    private final CellTable<ContentType> table;
    private final ControllableMultiSelectionModel<ContentType> selectionModel;
    private final ListDataProvider<ContentType> dataProvider;
    
    public FilterableSelectionTable() {
        listeners = new HashSet<>();
        
        allData = new ArrayList<ContentType>();
        
        DataMiningCellTableResources tableRes = GWT.create(DataMiningCellTableResources.class);
        table = new BaseCelltable<>(Integer.MAX_VALUE, tableRes);
        table.setWidth("100%");
        table.setAutoHeaderRefreshDisabled(true);
        table.setAutoFooterRefreshDisabled(true);
        
        TextColumn<ContentType> contentColumn = new TextColumn<ContentType>() {
            @Override
            public String getValue(ContentType content) {
                return FilterableSelectionTable.this.getElementAsString(content);
            }
        };
        contentColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        selectionModel = new ControllableMultiSelectionModel<>();
        final Column<ContentType, Boolean> checkColumn = new Column<ContentType, Boolean>(
                new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(ContentType object) {
                return selectionModel.isSelected(object);
            }
        };
        
        dataProvider = new ListDataProvider<ContentType>(new ProvidesKey<ContentType>() {
            @Override
            public Object getKey(ContentType item) {
                return getElementAsString(item);
            }
        });
        dataProvider.addDataDisplay(table);
        
        table.addColumn(checkColumn);
        table.addColumn(contentColumn);
        
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                notifyListeners();
            }
        });
        table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<ContentType> createCustomManager(
                new DefaultSelectionEventManager.CheckboxEventTranslator<ContentType>() {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<ContentType> event) {
                        return !isCheckboxColumn(event.getColumn());
                    }

                    @Override
                    public SelectAction translateSelectionEvent(CellPreviewEvent<ContentType> event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                            if (nativeEvent.getCtrlKey()) {
                                ContentType value = event.getValue();
                                selectionModel.setSelected(value, !selectionModel.isSelected(value));
                                return SelectAction.IGNORE;
                            }
                            if (!selectionModel.getSelectedSet().isEmpty() && !isCheckboxColumn(event.getColumn())) {
                                return SelectAction.DEFAULT;
                            }
                        }
                        return SelectAction.TOGGLE;
                    }

                    private boolean isCheckboxColumn(int columnIndex) {
                        return columnIndex == table.getColumnIndex(checkColumn);
                    }
                }));
        
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
        
        mainPanel = new FlowPanel();
        mainPanel.add(filterPanel);
        mainPanel.add(table);
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
            selectionModel.setBlockNotifications(true);
            clearSelection();
            selectionModel.setBlockNotifications(false);
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
        selectionModel.setBlockNotifications(true);
        
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

        selectionModel.setBlockNotifications(false);
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

    public HashSet<ContentType> getSelection() {
        return new HashSet<>(selectionModel.getSelectedSet());
    }

    public void setSelection(Iterable<?> elements, boolean notifyListenersWhenSelectionChanged) {
        try {
            @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
            Iterable<ContentType> specificContent = (Iterable<ContentType>) elements;
            selectionModel.setBlockNotifications(!notifyListenersWhenSelectionChanged);
            clearSelection();
            for (ContentType element : specificContent) {
                selectionModel.setSelected(element, true);
            }
            if (!notifyListenersWhenSelectionChanged) {
                selectionModel.setBlockNotifications(false);
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
        filterPanel.setVisible(visible);
        if (visible) {
            filterPanel.getTextBox().setFocus(true);
            filterPanel.getTextBox().selectAll();
        }
    }
    
    public void setVisible(boolean visible) {
        mainPanel.setVisible(visible);
    }
    
    public Widget getWidget() {
        return mainPanel;
    }

}
