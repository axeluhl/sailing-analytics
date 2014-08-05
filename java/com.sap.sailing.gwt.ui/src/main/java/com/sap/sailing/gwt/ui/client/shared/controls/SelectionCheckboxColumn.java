package com.sap.sailing.gwt.ui.client.shared.controls;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;

/**
 * A column to be used in a {@link CellTable} that controls and reflects a table's selection model. The column uses the
 * {@link BetterCheckboxCell} cell to implement the display properties. Three CSS styles can be used to parameterize this
 * column: one for the <code>&lt;td&gt;</code> element rendering the cell, and two for the <code>&lt;div&gt;</code> element
 * representing a selected or deselected element.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public abstract class SelectionCheckboxColumn<T> extends AbstractSortableColumnWithMinMax<T, Boolean> {
    private final BetterCheckboxCell cell;
    private final String checkboxColumnCellCSSClass;
    
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
    }
    
    /**
     * Subclass implementation has to tell whether the checkbox is selected or deselected for <code>row</code>
     */
    @Override
    public abstract Boolean getValue(T row);

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
}