package com.sap.sse.gwt.theme.client.component.celltable;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractCellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.DefaultHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sse.common.Util;

/**
 * Modification of {@link DefaultHeaderOrFooterBuilder} to add custom style names to the row element.
 * 
 * @param <T>
 *            the data type of the table
 */
public class StyledHeaderOrFooterBuilder<T> extends DefaultHeaderOrFooterBuilder<T> {

    private final String[] rowStyleNames;

    /**
     * Create a new HeaderOrFooterBuilderWithHeaderRowStyle for the header of footer section with custom row style
     * names.
     * 
     * @param table
     *            the table being built
     * @param isFooter
     *            true if building the footer, false if the header
     * @param rowStyleNames
     *            style names to add to the row element
     */
    public StyledHeaderOrFooterBuilder(AbstractCellTable<T> table, boolean isFooter, String... rowStyleNames) {
        super(table, isFooter);
        this.rowStyleNames = rowStyleNames;
    }

    @Override
    protected boolean buildHeaderOrFooterImpl() {
        AbstractCellTable<T> table = getTable();
        boolean isFooter = isBuildingFooter();

        // Early exit if there aren't any columns to render.
        int columnCount = table.getColumnCount();
        if (columnCount == 0) {
            // Nothing to render;
            return false;
        }

        // Early exit if there aren't any headers in the columns to render.
        boolean hasHeader = false;
        for (int i = 0; i < columnCount; i++) {
            if (getHeader(i) != null) {
                hasHeader = true;
                break;
            }
        }
        if (hasHeader == false) {
            return false;
        }

        // Get information about the sorted column.
        ColumnSortList sortList = table.getColumnSortList();
        ColumnSortInfo sortedInfo = (sortList.size() == 0) ? null : sortList.get(0);
        Column<?, ?> sortedColumn = (sortedInfo == null) ? null : sortedInfo.getColumn();
        boolean isSortAscending = (sortedInfo == null) ? false : sortedInfo.isAscending();

        // Get the common style names.
        Style style = getTable().getResources().style();
        String className = isBuildingFooter() ? style.footer() : style.header();
        String sortableStyle = " " + style.sortableHeader();
        String sortedStyle = " " + (isSortAscending ? style.sortedHeaderAscending() : style.sortedHeaderDescending());

        // Setup the first column.
        Header<?> prevHeader = getHeader(0);
        Column<T, ?> column = getTable().getColumn(0);
        int prevColspan = 1;
        boolean isSortable = false;
        boolean isSorted = false;
        StringBuilder classesBuilder = new StringBuilder(className);
        classesBuilder.append(" " + (isFooter ? style.firstColumnFooter() : style.firstColumnHeader()));
        if (!isFooter && column.isSortable()) {
            isSortable = true;
            isSorted = (column == sortedColumn);
        }

        // Loop through all column headers.
        TableRowBuilder tr = startRow();
        if (this.rowStyleNames.length > 0) {
            tr.className(Util.join(" ", rowStyleNames));
        }
        int curColumn;
        for (curColumn = 1; curColumn < columnCount; curColumn++) {
            Header<?> header = getHeader(curColumn);

            if (header != prevHeader) {
                // The header has changed, so append the previous one.
                if (isSortable) {
                    classesBuilder.append(sortableStyle);
                }
                if (isSorted) {
                    classesBuilder.append(sortedStyle);
                }
                appendExtraStyles(prevHeader, classesBuilder);

                // Render the header.
                TableCellBuilder th = tr.startTH().colSpan(prevColspan).className(classesBuilder.toString());
                enableColumnHandlers(th, column);
                if (prevHeader != null) {
                    // Build the header.
                    Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
                    // Add div element with aria button role
                    if (isSortable) {
                        // TODO: Figure out aria-label and translation of label text
                        th.attribute("role", "button");
                        th.tabIndex(-1);
                    }
                    renderSortableHeader(th, context, prevHeader, isSorted, isSortAscending);
                }
                th.endTH();

                // Reset the previous header.
                prevHeader = header;
                prevColspan = 1;
                classesBuilder = new StringBuilder(className);
                isSortable = false;
                isSorted = false;
            } else {
                // Increment the colspan if the headers == each other.
                prevColspan++;
            }

            // Update the sorted state.
            column = table.getColumn(curColumn);
            if (!isFooter && column.isSortable()) {
                isSortable = true;
                isSorted = (column == sortedColumn);
            }
        }

        // Append the last header.
        if (isSortable) {
            classesBuilder.append(sortableStyle);
        }
        if (isSorted) {
            classesBuilder.append(sortedStyle);
        }

        // The first and last columns could be the same column.
        classesBuilder.append(" ").append(isFooter ? style.lastColumnFooter() : style.lastColumnHeader());
        appendExtraStyles(prevHeader, classesBuilder);

        // Render the last header.
        TableCellBuilder th = tr.startTH().colSpan(prevColspan).className(classesBuilder.toString());
        enableColumnHandlers(th, column);
        if (prevHeader != null) {
            Context context = new Context(0, curColumn - prevColspan, prevHeader.getKey());
            renderSortableHeader(th, context, prevHeader, isSorted, isSortAscending);
        }
        th.endTH();

        // End the row.
        tr.endTR();

        return true;
    }

    /**
     * Append the extra style names for the header.
     * 
     * @param header
     *            the header that may contain extra styles, it can be null
     * @param classesBuilder
     *            the string builder for the TD classes
     */
    private <H> void appendExtraStyles(Header<H> header, StringBuilder classesBuilder) {
        if (header == null) {
            return;
        }
        String headerStyleNames = header.getHeaderStyleNames();
        if (headerStyleNames != null) {
            classesBuilder.append(" ");
            classesBuilder.append(headerStyleNames);
        }
    }
}
