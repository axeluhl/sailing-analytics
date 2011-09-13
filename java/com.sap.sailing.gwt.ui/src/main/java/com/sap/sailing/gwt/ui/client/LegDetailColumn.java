package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class LegDetailColumn<T> extends SortableColumn<LeaderboardRowDAO, String> {
    private final String title;
    private final LegDetailField<T> field;
    
    public interface LegDetailField<T> {
        T get(LeaderboardRowDAO row);
    }
    
    protected LegDetailColumn(String title, LegDetailField<T> field) {
        super(new TextCell());
        setHorizontalAlignment(ALIGN_RIGHT);
        this.title = title;
        this.field = field;
    }

    protected String getTitle() {
        return title;
    }

    protected LegDetailField<T> getField() {
        return field;
    }

    @Override
    public Comparator<LeaderboardRowDAO> getComparator() {
        return new Comparator<LeaderboardRowDAO>() {
            @Override
            public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                try {
                    @SuppressWarnings("unchecked")
                    Comparable<T> value1 = (Comparable<T>) field.get(o1);
                    T value2 = field.get(o2);
                    return value1 == null ? value2 == null ? 0 : -1 : value2 == null ? 1 : value1.compareTo(value2);
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

    @Override
    public String getValue(LeaderboardRowDAO row) {
        T fieldValue = field.get(row);
        return fieldValue == null ? "" : fieldValue.toString();
    }
}
