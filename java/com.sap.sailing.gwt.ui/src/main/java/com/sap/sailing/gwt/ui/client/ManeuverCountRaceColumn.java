package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.Detail;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField;
import com.sap.sailing.gwt.ui.client.ManeuverColumn.AbstractManeuverDetailField;
import com.sap.sailing.gwt.ui.client.ManeuverDetailColumn.ManeuverDetailField;
import com.sap.sailing.gwt.ui.shared.DetailType;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class ManeuverCountRaceColumn extends ExpandableSortableColumn<String> {

    private final StringConstants stringConstants;
    private final String raceName;
    
    private final String headerStyle;
    private final String columnStyle;

    private abstract class AbstractManeuverDetailField<T extends Comparable<?>> implements LegDetailField<T> {
        public T get(LeaderboardRowDAO row) {
            LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
            if (fieldsForRace == null) {
                return null;
            } else {
                return getFromNonNullEntry(fieldsForRace);
            }
        }

        protected abstract T getFromNonNullEntry(LeaderboardEntryDAO entry);
    }

    private class NumberOfTacks extends AbstractManeuverDetailField<Integer> {

        @Override
        protected Integer getFromNonNullEntry(LeaderboardEntryDAO entry) {
            // TODO get lerderboard double like in get number of
            return ManeuverCountRaceColumn.this.getTotalNumberOfTacks(entry);
        }
    }
    
    private class NumberOfJibes extends AbstractManeuverDetailField<Integer> {

        @Override
        protected Integer getFromNonNullEntry(LeaderboardEntryDAO entry) {
            // TODO get lerderboard double like in get number of
            return ManeuverCountRaceColumn.this.getTotalNumberOfJibes(entry);
        }
    }
    
    private class NumberOfPenaltyCircles extends AbstractManeuverDetailField<Integer> {

        @Override
        protected Integer getFromNonNullEntry(LeaderboardEntryDAO entry) {
            // TODO get lerderboard double like in get number of
            return ManeuverCountRaceColumn.this.getTotalNumberOfPenaltyCircles(entry);
        }
    }
    
    public ManeuverCountRaceColumn(LeaderboardPanel leaderboardPanel, String raceName, StringConstants stringConstants,
            List<DetailType> maneuverDetailSelection, String headerStyle, String columnStylee) {
        super(leaderboardPanel, /* expandable */true /* all legs have details */, new TextCell(), stringConstants,
                headerStyle, columnStylee, maneuverDetailSelection);
        this.stringConstants = stringConstants;
        this.raceName = raceName;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStylee;
    }

    private Triple<Integer, Integer, Integer> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardRowDAO row) {
        Integer totalNumberOfTacks = null;
        Integer totalNumberOfJibes = null;
        Integer totalNumberOfPenaltyCircles = null;
        LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
        if (fieldsForRace != null && fieldsForRace.legDetails != null) {
            for (LegEntryDAO legDetail : fieldsForRace.legDetails) {
                if (legDetail != null) {
                    if (legDetail.numberOfTacks != null) {
                        if (totalNumberOfTacks == null) {
                            totalNumberOfTacks = legDetail.numberOfTacks;
                        } else {
                            totalNumberOfTacks += legDetail.numberOfTacks;
                        }
                    }
                    if (legDetail.numberOfJibes != null) {
                        if (totalNumberOfJibes == null) {
                            totalNumberOfJibes = legDetail.numberOfJibes;
                        } else {
                            totalNumberOfJibes += legDetail.numberOfJibes;
                        }
                    }
                    if (legDetail.numberOfPenaltyCircles != null) {
                        if (totalNumberOfPenaltyCircles == null) {
                            totalNumberOfPenaltyCircles = legDetail.numberOfPenaltyCircles;
                        } else {
                            totalNumberOfPenaltyCircles += legDetail.numberOfPenaltyCircles;
                        }
                    }
                }
            }
        }
        return new Triple<Integer, Integer, Integer>(totalNumberOfTacks, totalNumberOfJibes,
                totalNumberOfPenaltyCircles);
    }

    private String getRaceName() {
        return raceName;
    }

    @Override
    public Header<SafeHtml> getHeader() {
        SortableExpandableColumnHeader result = new SortableExpandableColumnHeader(/* title */ stringConstants.maneuverTypes(),
                /* iconURL */ null, getLeaderboardPanel(), this, stringConstants);
        return result;
    }

    @Override
    public Comparator<LeaderboardRowDAO> getComparator() {
         return new Comparator<LeaderboardRowDAO>() {
            @Override
            public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                boolean ascending = isSortedAscendingForThisColumn(getLeaderboardPanel().getLeaderboardTable());
                if(o1 != null && o2 != null){
                    String val1 = getValue(o1);
                    String val2 = getValue(o2);
                    if(val1!="" && val1 != null && val2 != "" && val2 != null){
                        int diff = Integer.parseInt(val2) - Integer.parseInt(val1);
                        return ascending ? diff : -diff;
                    }
                }
                return 0;
            }
        };
    }

    @Override
    public String getValue(LeaderboardRowDAO object) {
        Double result = null;
        Triple<Integer, Integer, Integer> tacksJibesAndPenalties = getTotalNumberOfTacksJibesAndPenaltyCircles(object);
        Integer totalNumberOfTacks = tacksJibesAndPenalties.getA();
        Integer totalNumberOfJibes = tacksJibesAndPenalties.getB();
        Integer totalNumberOfPenaltyCircles = tacksJibesAndPenalties.getC();
        if (totalNumberOfTacks != null) {
            result = (double) totalNumberOfTacks;
        }
        if (totalNumberOfJibes != null) {
            if (result == null) {
                result = (double) totalNumberOfJibes;
            } else {
                result += (double) totalNumberOfJibes;
            }
        }
        if (totalNumberOfPenaltyCircles != null) {
            if (result == null) {
                result = (double) totalNumberOfPenaltyCircles;
            } else {
                result += (double) totalNumberOfPenaltyCircles;
            }
        }
        if(result==null){
            return "";
        }else{
            return result.toString();
        }
    }
    
    @Override
    protected Map<DetailType, SortableColumn<LeaderboardRowDAO, ?>> getDetailColumnMap(
            LeaderboardPanel leaderboardPanel, StringConstants stringConstants, String detailHeaderStyle,
            String detailColumnStyle) {
        Map<DetailType, SortableColumn<LeaderboardRowDAO, ?>> result = new HashMap<DetailType, SortableColumn<LeaderboardRowDAO, ?>>();
        result.put(DetailType.HEAD_UP, new )
        return result;
    }
    
    public static DetailType[] getAvailableManeuverDetailColumnTypes() {
        return new DetailType[] { DetailType.TACK, DetailType.JIBE, DetailType.PENALTY_CIRCLE};
    }
    
    @Override
    public String getColumnStyle() {
        return columnStyle;
    }
    
    @Override
    public String getHeaderStyle() {
        return headerStyle;
    }

}
