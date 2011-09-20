package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public abstract class LegDetailColumn<FieldType extends Comparable<?>, RenderingType> extends SortableColumn<LeaderboardRowDAO, RenderingType> {
    private final String title;
    private final LegDetailField<FieldType> field;
    private final CellTable<LeaderboardRowDAO> leaderboardTable;
    private FieldType minimum;
    private FieldType maximum;
    
    public interface LegDetailField<T extends Comparable<?>> {
        T get(LeaderboardRowDAO row);
    }
    
    protected LegDetailColumn(String title, LegDetailField<FieldType> field, Cell<RenderingType> cell, CellTable<LeaderboardRowDAO> leaderboardTable) {
        super(cell);
        setHorizontalAlignment(ALIGN_RIGHT);
        this.title = title;
        this.field = field;
        this.leaderboardTable = leaderboardTable;
    }

    protected String getTitle() {
        return title;
    }

    protected LegDetailField<FieldType> getField() {
        return field;
    }

    @Override
    public Comparator<LeaderboardRowDAO> getComparator() {
        return new Comparator<LeaderboardRowDAO>() {
            @Override
            public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                boolean ascending = isSortedAscendingForThisColumn(leaderboardTable);
                try {
                    @SuppressWarnings("unchecked")
                    Comparable<FieldType> value1 = (Comparable<FieldType>) field.get(o1);
                    FieldType value2 = field.get(o2);
                    return value1 == null ? value2 == null ? 0 : ascending?1:-1
                            : value2 == null ? ascending?-1:1 : value1.compareTo(value2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public Header<?> getHeader() {
        return new TextHeader(title);
    }

    public FieldType getMinimum() {
        return minimum;
    }
    
    public FieldType getMaximum() {
        return maximum;
    }

    @Override
    protected void updateMinMax(LeaderboardDAO leaderboard) {
        Comparator<LeaderboardRowDAO> comparator = getComparator();
        LeaderboardRowDAO minimumRow = null;
        LeaderboardRowDAO maximumRow = null;
        for (LeaderboardRowDAO row : leaderboard.rows.values()) {
            if (minimumRow == null || (comparator.compare(minimumRow, row) > 0 && getField().get(row) != null)) {
                minimumRow = row;
            }
            if (maximumRow == null || (comparator.compare(maximumRow, row) < 0 && getField().get(row) != null)) {
                maximumRow = row;
            }
        }
        if (minimumRow != null) {
            minimum = getField().get(minimumRow);
        }
        if (maximumRow != null) {
            maximum = getField().get(maximumRow);
        }
    }
    
}
