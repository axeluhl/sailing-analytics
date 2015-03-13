package com.sap.sailing.gwt.ui.client.shared.controls;

import java.util.List;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;

/**
 * A column to be used in a {@link CellTable} that controls and reflects a table's selection model using stylable
 * "check boxes". To make things work, clients have to also call
 * {@link CellTable#setSelectionModel(com.google.gwt.view.client.SelectionModel, com.google.gwt.view.client.CellPreviewEvent.Handler)}
 * with the result of this columns {@link #getSelectionModel} as the first and the result of the
 * {@link #getSelectionManager()} method as second argument. This will ensure that the event handling and selection
 * updates work properly.
 * <p>
 * 
 * If clients don't choose to use this columns own {@link #getSelectionModel() selection model}, they have to ensure
 * that selection changes with subsequent {@link #redrawRow(Object, List) redraw requests} are triggered properly upon
 * selection status changes. Otherwise, the checkboxes will run out of sync with the selection status.
 * <p>
 * 
 * The column uses the {@link BetterCheckboxCell} cell to implement the display properties. Three CSS styles can be used
 * to parameterize this column: one for the <code>&lt;td&gt;</code> element rendering the cell, and two for the
 * <code>&lt;div&gt;</code> element representing a selected or deselected element.
 * 
 * @author Axel Uhl (D043530)
 * 
 * @param <T>
 */
public abstract class SelectionCheckboxColumn<T> extends AbstractSortableColumnWithMinMax<T, Boolean> {
    private final BetterCheckboxCell cell;
    private final String checkboxColumnCellCSSClass;
    private final EventTranslator<T> selectionEventTranslator;
    private final MultiSelectionModel<T> selectionModel;
    
    /**
     * @param selectedCheckboxCSSClass
     *            CSS class for the <code>&lt;div&gt;</code> element representing a selected element
     * @param deselectedCheckboxCSSClass
     *            CSS class for the <code>&lt;div&gt;</code> element representing a deselected element
     * @param checkboxColumnCellCSSClass
     *            CSS class for the <code>&lt;td&gt;</code> element rendering the cell
     */
    protected SelectionCheckboxColumn(String selectedCheckboxCSSClass,
            String deselectedCheckboxCSSClass, String checkboxColumnCellCSSClass) {
        this(new BetterCheckboxCell(selectedCheckboxCSSClass, deselectedCheckboxCSSClass), checkboxColumnCellCSSClass);
    }

    private SelectionCheckboxColumn(BetterCheckboxCell checkboxCell, String checkboxColumnCellCSSClass) {
        super(checkboxCell, SortingOrder.DESCENDING);
        this.cell = checkboxCell;
        this.checkboxColumnCellCSSClass = checkboxColumnCellCSSClass;
        this.selectionEventTranslator = createSelectionEventTranslator();
        this.selectionModel = createSelectionModel();
    }
    
    /**
     * Subclasses need to tell this column what the data list is. This is necessary in order to reach all rows after the
     * selection is cleared, as well as to trigger the redraw of a row by setting it again in the list data provider which
     * triggers the necessary redraw operation.
     */
    protected abstract ListDataProvider<T> getListDataProvider();
    
    /**
     * @return a selection manager that should be used for the table to which this column is added; use
     *         {@link CellTable#setSelectionModel(com.google.gwt.view.client.SelectionModel, com.google.gwt.view.client.CellPreviewEvent.Handler)}
     *         to set the selection manager together with the selection model on the table.
     */
    public CellPreviewEvent.Handler<T> getSelectionManager() {
        return DefaultSelectionEventManager.createCustomManager(getSelectionEventTranslator());
    }

    public MultiSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    /**
     * Clients should use the multi-selection model returned by this method for the {@link CellTable} to which they add this column.
     * If they do so, the {@link #redrawRow(LeaderboardRowDTO, List)} method will be triggered correctly for all selection changes.
     * Otherwise, clients or subclasses are responsible to issue the necessary calls to {@link #redrawRow(LeaderboardRowDTO, List)}
     * after selection changes.
     */
    private MultiSelectionModel<T> createSelectionModel() {
        return new MultiSelectionModel<T>() {
            @Override
            public void clear() {
                super.clear();
                final List<T> list = getListDataProvider().getList();
                for (T item : list) {
                    redrawRow(item, list);
                }
            }

            @Override
            public void setSelected(T item, boolean selected) {
                final boolean redrawRequired = isSelected(item) != selected;
                super.setSelected(item, selected);
                if (redrawRequired) {
                    redrawRow(item, getListDataProvider().getList());
                }
            }
            
        };
    }
    
    /**
     * @return a selection event translator that works nicely with
     *         {@link DefaultSelectionEventManager#createCustomManager(EventTranslator)} to ensure that this selection
     *         checkbox column does what it is supposed to do. Client may want to obtain the custom event manager
     *         directly by using {@link #getSelectionManager()}.
     */
    public EventTranslator<T> getSelectionEventTranslator() {
        return selectionEventTranslator;
    }

    /**
     * The checkbox value is determined by by selection state of <code>row</code>, using the
     * {@link #getSelectionModel() selection model}.
     */
    @Override
    public Boolean getValue(T row) {
        return getSelectionModel().isSelected(row);
    }

    /**
     * The default header display is a check mark. Subclasses may redefine this.
     */
    @Override
    public Header<?> getHeader() {
        return new SafeHtmlHeader(new SafeHtmlBuilder().appendEscaped("\u2713").toSafeHtml());
    }

    @Override
    public String getCellStyleNames(Context context, T object) {
        String basicStyles = super.getCellStyleNames(context, object);
        return basicStyles == null ? checkboxColumnCellCSSClass : (basicStyles + " " + checkboxColumnCellCSSClass);
    }

    @Override
    public BetterCheckboxCell getCell() {
        return cell;
    }

    @Override
    public InvertibleComparator<T> getComparator() {
        return new InvertibleComparatorAdapter<T>() {
            @Override
            public int compare(T a, T b) {
                return getValue(a) ? getValue(b) ? 0 : 1 : getValue(b) ? -1 : 0;
            }
        };
    }

    /**
     * No reasonable min/max display for this column; does nothing.
     */
    @Override
    public void updateMinMax() {}
    
    private EventTranslator<T> createSelectionEventTranslator() {
        return new EventTranslator<T>() {
            /**
             * Don't clear the selection when the user has clicked on the checkbox column
             */
            @Override
            public boolean clearCurrentSelection(CellPreviewEvent<T> event) {
                NativeEvent nativeEvent = event.getNativeEvent();
                boolean ctrlOrMeta = nativeEvent.getCtrlKey() || nativeEvent.getMetaKey();
                return !isSelectionCheckboxColumn(event) && !ctrlOrMeta;
            }

            private boolean isSelectionCheckboxColumn(CellPreviewEvent<T> event) {
                Column<?, ?> column = getColumn(event);
                return column == SelectionCheckboxColumn.this;
            }
            
            private Column<?, ?> getColumn(CellPreviewEvent<T> event) {
                CellTable<T> table = (CellTable<T>) event.getDisplay();
                return table.getColumn(event.getContext().getColumn());
            }

            @Override
            public SelectAction translateSelectionEvent(CellPreviewEvent<T> event) {
                final SelectAction result;
                if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType()) && isSelectionCheckboxColumn(event)) {
                    result = SelectAction.TOGGLE;
                } else {
                    result = SelectAction.DEFAULT;
                }
                return result;
            }
        };
    }

    /**
     * Subclasses shall use this after an update to the selection state of a row to ensure the checkboxes are
     * displayed correctly and consistently again.
     * 
     * @param row
     *            the row object to be re-drawn
     * @param dataList
     *            the result of calling {@link ListDataProvider#getList()} on the table's data provider which is
     *            actually a list wrapper where updates to which are reflected in the table display
     */
    protected void redrawRow(final T row, final List<T> dataList) {
        int rowIndex = dataList.indexOf(row);
        if (rowIndex >= 0) {
            dataList.set(rowIndex, row); // trigger row redraw to have check box shown in correct state
        }
        getListDataProvider().flush();
    }
}