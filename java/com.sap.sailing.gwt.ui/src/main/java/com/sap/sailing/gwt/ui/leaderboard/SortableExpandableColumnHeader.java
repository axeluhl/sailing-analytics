package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.SafeImageCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * A {@link CellTable} {@link Header} implementation that uses a {@link CompositeCell} containing a
 * {@link TextCell} and optionally an {@link ActionCell} for an expand/close button and an {@link ImageCell}
 * for a medal displayed for medal races.
 */
public class SortableExpandableColumnHeader extends Header<SafeHtml> {
    private static class ExpandCollapseButtonAction implements ActionCell.Delegate<SafeHtml> {
        private final ExpandableSortableColumn<?> column;

        private ExpandCollapseButtonAction(ExpandableSortableColumn<?> column) {
            this.column = column;
        }

        @Override
        public void execute(SafeHtml object) {
            column.changeExpansionState(/* expand */ !column.isExpanded());
        }
    }

    interface CellTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<span class=\"race-name\" title=\"{1}\">{0}</span>")
        SafeHtml cellWithTooltip(SafeHtml title, String tooltip);
        
        @SafeHtmlTemplates.Template("<span class=\"race-name\" title=\"{1}\">{0}<br>[{2}]</span>")
        SafeHtml cellWithTooltipAndUnit(SafeHtml title, String tooltip, String unit);

        @SafeHtmlTemplates.Template("<span class=\"race-name\">{0}</span>")
        SafeHtml cell(SafeHtml title);
    }
    
    private static CellTemplates template = GWT.create(CellTemplates.class);
    
    public SortableExpandableColumnHeader(String title, SafeUri iconURL, LeaderboardPanel<?> leaderboardPanel,
    ExpandableSortableColumn<?> column, StringMessages stringConstants) {
        this(title, null, null, iconURL, leaderboardPanel, column, stringConstants);
    }

    public SortableExpandableColumnHeader(String title, String tooltip, SafeUri iconURL,
    LeaderboardPanel<?> leaderboardPanel, ExpandableSortableColumn<?> column, StringMessages stringConstants) {
        this(title, tooltip, null, iconURL, leaderboardPanel, column, stringConstants);
    }

    public SortableExpandableColumnHeader(String title, String tooltip, SafeUri iconURL,
    LeaderboardPanel<?> leaderboardPanel, ExpandableSortableColumn<?> column, StringMessages stringConstants, String unit) {
        this(title, tooltip, unit, iconURL, leaderboardPanel, column, stringConstants);
    }

    public SortableExpandableColumnHeader(String title, String tooltip, String unit,
            SafeUri iconURL, LeaderboardPanel<?> leaderboardPanel, ExpandableSortableColumn<?> column, StringMessages stringConstants) {
        super(constructCell(title, tooltip, unit, iconURL, column.isExpansionEnabled(), leaderboardPanel, column, stringConstants));
    }

    private static <T> Cell<SafeHtml> constructCell(final String title, final String tooltip, final String unit,
            final SafeUri iconURL, boolean isExpansionEnabled, final LeaderboardPanel<?> leaderboardPanel, final ExpandableSortableColumn<?> column, final StringMessages stringConstants) {
        final List<HasCell<SafeHtml, ?>> cells = new ArrayList<HasCell<SafeHtml, ?>>(3);
        // if it's a medal race, add the cell rendering the medal image
        // add the cell rendering the expand/collapse button:
        if (isExpansionEnabled) {
            cells.add(new HasCell<SafeHtml, SafeHtml>() {
                @Override
                public Cell<SafeHtml> getCell() {
                    return new ExpandCollapseButtonCell(column, new ExpandCollapseButtonAction(column));
                }

                @Override
                public FieldUpdater<SafeHtml, SafeHtml> getFieldUpdater() {
                    return null; // no updates possible in a header cell
                }

                @Override
                public SafeHtml getValue(SafeHtml object) {
                    return null;
                }
            });
        }
        if (iconURL != null) {
            cells.add(new HasCell<SafeHtml, SafeUri>() {
                @Override
                public Cell<SafeUri> getCell() {
                    return new SafeImageCell();
                }

                @Override
                public FieldUpdater<SafeHtml, SafeUri> getFieldUpdater() {
                    return null; // no updates possible in a header cell
                }

                @Override
                public SafeUri getValue(SafeHtml object) {
                    return iconURL;
                }
            });
        }
        // add the cell rendering the race name:
        cells.add(new HasCell<SafeHtml, SafeHtml>() {
            @Override
            public Cell<SafeHtml> getCell() {
                return new SafeHtmlCell();
            }
            @Override
            public FieldUpdater<SafeHtml, SafeHtml> getFieldUpdater() {
                return null; // no updates possible in a header cell
            }
            @Override
            public SafeHtml getValue(SafeHtml object) {
                String tooltipText = tooltip != null ? tooltip : "";
                if (unit == null) {
                    return template.cellWithTooltip(SafeHtmlUtils.fromString(title), SafeHtmlUtils.htmlEscape(tooltipText));
                } else {
                    return template.cellWithTooltipAndUnit(SafeHtmlUtils.fromString(title), SafeHtmlUtils.htmlEscape(tooltipText), SafeHtmlUtils.htmlEscape(unit));
                }
            }
        });
        
        return new CompositeCell<SafeHtml>(cells);
    }

    @Override
    public SafeHtml getValue() {
        return null;
    }
    
}

