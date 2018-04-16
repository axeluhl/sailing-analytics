package com.sap.sailing.android.tracking.app.utils;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Checkin;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.tracking.app.provider.AnalyticsDatabase;
import com.sap.sailing.android.tracking.app.valueobjects.BoatCheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.BoatInfo;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorCheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.android.tracking.app.valueobjects.MarkCheckinData;
import com.sap.sailing.android.tracking.app.valueobjects.MarkInfo;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Mark;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class DatabaseHelper {

    private final static String TAG = DatabaseHelper.class.getName();

    private static DatabaseHelper mInstance;

    public static synchronized DatabaseHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseHelper();
        }

        return mInstance;
    }

    public List<String> getCheckinUrls(Context context) {
        List<String> checkinUrls = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Checkin.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String checkinUrl = cursor.getString(cursor.getColumnIndex(Checkin.CHECKIN_URI_VALUE));
                if (!checkinUrls.contains(checkinUrl)) {
                    checkinUrls.add(checkinUrl);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        return checkinUrls;
    }

    public EventInfo getEventInfo(Context context, String checkinDigest) {
        EventInfo event = new EventInfo();
        event.checkinDigest = checkinDigest;

        Cursor cursor = context.getContentResolver().query(Event.CONTENT_URI, null,
                Event.EVENT_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                event.name = cursor.getString(cursor.getColumnIndex(Event.EVENT_NAME));
                event.imageUrl = cursor.getString(cursor.getColumnIndex(Event.EVENT_IMAGE_URL));
                event.startMillis = cursor.getLong(cursor.getColumnIndex(Event.EVENT_DATE_START));
                event.endMillis = cursor.getLong(cursor.getColumnIndex(Event.EVENT_DATE_END));
                event.server = cursor.getString(cursor.getColumnIndex(Event.EVENT_SERVER));
                event.rowId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                event.id = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID));
            }

            cursor.close();
        }
        return event;
    }

    public CompetitorInfo getCompetitor(Context context, String checkinDigest) {
        CompetitorInfo competitor = new CompetitorInfo();
        competitor.checkinDigest = checkinDigest;

        Cursor cursor = context.getContentResolver().query(Competitor.CONTENT_URI, null,
                Competitor.COMPETITOR_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                competitor.name = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_DISPLAY_NAME));
                competitor.countryCode = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_COUNTRY_CODE));
                competitor.sailId = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_SAIL_ID));
                competitor.rowId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                competitor.id = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_ID));
            }

            cursor.close();
        }
        return competitor;
    }

    public LeaderboardInfo getLeaderboard(Context context, String checkinDigest) {
        LeaderboardInfo leaderboard = new LeaderboardInfo();
        leaderboard.checkinDigest = checkinDigest;

        Cursor cursor = context.getContentResolver().query(Leaderboard.CONTENT_URI, null,
                Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                leaderboard.rowId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                leaderboard.name = cursor.getString(cursor.getColumnIndex(Leaderboard.LEADERBOARD_NAME));
                leaderboard.displayName = cursor.getString(cursor.getColumnIndex(Leaderboard.LEADERBOARD_DISPLAY_NAME));
            }

            cursor.close();
        }

        return leaderboard;
    }

    public CheckinUrlInfo getCheckinUrl(Context context, String checkinDigest) {
        CheckinUrlInfo checkinUrlInfo = new CheckinUrlInfo();
        checkinUrlInfo.checkinDigest = checkinDigest;

        Cursor uc = context.getContentResolver().query(Checkin.CONTENT_URI, null,
                Checkin.CHECKIN_URI_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (uc != null) {
            if (uc.moveToFirst()) {
                checkinUrlInfo.rowId = uc.getInt(uc.getColumnIndex(BaseColumns._ID));
                checkinUrlInfo.urlString = uc.getString(uc.getColumnIndex(Checkin.CHECKIN_URI_VALUE));
                checkinUrlInfo.type = uc.getInt(uc.getColumnIndex(Checkin.CHECKIN_TYPE));
            }

            uc.close();
        }

        return checkinUrlInfo;
    }

    public MarkInfo getMarkInfo(Context context, String checkinDigest) {
        MarkInfo markInfo = new MarkInfo();
        markInfo.checkinDigest = checkinDigest;
        Cursor markCursor = context.getContentResolver().query(AnalyticsContract.Mark.CONTENT_URI, null,
            AnalyticsContract.Mark.MARK_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (markCursor != null) {
            if (markCursor.moveToFirst()) {
                markInfo.markId = markCursor.getString(markCursor.getColumnIndex(AnalyticsContract.Mark.MARK_ID));
                markInfo.markName = markCursor.getString(markCursor.getColumnIndex(AnalyticsContract.Mark.MARK_NAME));
            }

            markCursor.close();
        }
        return markInfo;
    }

    public BoatInfo getBoatInfo(Context context, String checkinDigest) {
        BoatInfo boatInfo = new BoatInfo();
        boatInfo.checkinDigest = checkinDigest;
        Cursor boatCursor = context.getContentResolver().query(AnalyticsContract.Boat.CONTENT_URI, null,
            AnalyticsContract.Boat.BOAT_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (boatCursor != null) {
            if (boatCursor.moveToFirst()) {
                boatInfo.boatId = boatCursor.getString(boatCursor.getColumnIndex(AnalyticsContract.Boat.BOAT_ID));
                boatInfo.boatName = boatCursor.getString(boatCursor.getColumnIndex(AnalyticsContract.Boat.BOAT_NAME));
                boatInfo.boatColor = boatCursor.getString(boatCursor.getColumnIndex(AnalyticsContract.Boat.BOAT_COLOR));
            }
            boatCursor.close();
        }
        return boatInfo;
    }

    public void deleteRegattaFromDatabase(Context context, String checkinDigest) {
        ContentResolver cr = context.getContentResolver();

        int d1 = cr.delete(Event.CONTENT_URI, Event.EVENT_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d2 = cr.delete(Competitor.CONTENT_URI, Competitor.COMPETITOR_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d3 = cr.delete(Leaderboard.CONTENT_URI, Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d4 = cr.delete(Checkin.CONTENT_URI, Checkin.CHECKIN_URI_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d5 = cr.delete(AnalyticsContract.Mark.CONTENT_URI, AnalyticsContract.Mark.MARK_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d6 = cr.delete(AnalyticsContract.Boat.CONTENT_URI, AnalyticsContract.Boat.BOAT_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });

        if (BuildConfig.DEBUG) {
            ExLog.i(context, TAG, "Checkout, number of events deleted: " + d1);
            ExLog.i(context, TAG, "Checkout, number of competitors deleted: " + d2);
            ExLog.i(context, TAG, "Checkout, number of leaderboards deleted: " + d3);
            ExLog.i(context, TAG, "Checkout, number of checkinurls deleted: " + d4);
            ExLog.i(context, TAG, "Checkout, number of marks deleted: " + d5);
            ExLog.i(context, TAG, "Checkout, number of boats deleted: " + d6);
        }
    }

    /**
     * When checking in, store info on the event, the competitor and the leaderboard in the database.
     *
     * @param context android context
     * @return success or failure
     * @throws GeneralDatabaseHelperException
     */
    public void storeCompetitorCheckinRow(Context context, CompetitorCheckinData checkin) throws GeneralDatabaseHelperException {
        EventInfo event = checkin.getEvent();
        CompetitorInfo competitor = checkin.getCompetitor();
        LeaderboardInfo leaderboard = checkin.getLeaderboard();
        CheckinUrlInfo checkinURL = checkin.getCheckinUrl();

        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> opList = new ArrayList<>();

        // competitor
        ContentValues cv = new ContentValues();
        cv.put(Competitor.COMPETITOR_COUNTRY_CODE, competitor.countryCode);
        cv.put(Competitor.COMPETITOR_DISPLAY_NAME, competitor.name);
        cv.put(Competitor.COMPETITOR_ID, competitor.id);
        cv.put(Competitor.COMPETITOR_NATIONALITY, competitor.nationality);
        cv.put(Competitor.COMPETITOR_SAIL_ID, competitor.sailId);
        cv.put(Competitor.COMPETITOR_CHECKIN_DIGEST, competitor.checkinDigest);

        opList.add(ContentProviderOperation.newInsert(Competitor.CONTENT_URI).withValues(cv).build());
        addBasicInformationToOperationList(opList, leaderboard, event, checkinURL);

        try {
            contentResolver.applyBatch(AnalyticsContract.CONTENT_AUTHORITY, opList);
        } catch (RemoteException| OperationApplicationException e) {
            throw new GeneralDatabaseHelperException(e.getMessage());
        }
    }

    public void storeMarkCheckinRow(Context context, MarkCheckinData checkin) throws GeneralDatabaseHelperException {
        EventInfo event = checkin.getEvent();
        Mark mark = checkin.getMark();
        LeaderboardInfo leaderboard = checkin.getLeaderboard();
        CheckinUrlInfo checkinURL = checkin.getCheckinUrl();

        // Store Mark information
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> opList = new ArrayList<>();
        ContentValues cv = new ContentValues();
        cv.put(AnalyticsContract.Mark.MARK_ID, mark.getId().toString());
        cv.put(AnalyticsContract.Mark.MARK_NAME, mark.getName());
        cv.put(AnalyticsContract.Mark.MARK_CHECKIN_DIGEST, checkinURL.checkinDigest);
        opList.add(ContentProviderOperation.newInsert(AnalyticsContract.Mark.CONTENT_URI).withValues(cv).build());

        addBasicInformationToOperationList(opList, leaderboard, event, checkinURL);

        try {
            contentResolver.applyBatch(AnalyticsContract.CONTENT_AUTHORITY, opList);
        } catch (RemoteException| OperationApplicationException e) {
            throw new GeneralDatabaseHelperException(e.getMessage());
        }
    }

    public void storeBoatCheckinRow(Context context, BoatCheckinData checkin) throws GeneralDatabaseHelperException {
        EventInfo event = checkin.getEvent();
        LeaderboardInfo leaderboard = checkin.getLeaderboard();
        CheckinUrlInfo checkinUrlInfo = checkin.getCheckinUrl();
        Boat boat = checkin.getBoat();
        // boat
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> opList = new ArrayList<>();
        ContentValues cv = new ContentValues();
        cv.put(AnalyticsContract.Boat.BOAT_ID, boat.getId().toString());
        if (TextUtils.isEmpty(boat.getName())) {
            cv.put(AnalyticsContract.Boat.BOAT_NAME, boat.getSailID());
        } else {
            cv.put(AnalyticsContract.Boat.BOAT_NAME, boat.getName());
        }
        cv.put(AnalyticsContract.Boat.BOAT_CHECKIN_DIGEST, checkinUrlInfo.checkinDigest);
        cv.put(AnalyticsContract.Boat.BOAT_COLOR, boat.getColor()==null?null:boat.getColor().getAsHtml());
        opList.add(ContentProviderOperation.newInsert(AnalyticsContract.Boat.CONTENT_URI).withValues(cv).build());
        addBasicInformationToOperationList(opList, leaderboard, event, checkinUrlInfo);
        try {
            contentResolver.applyBatch(AnalyticsContract.CONTENT_AUTHORITY, opList);
        } catch (RemoteException | OperationApplicationException e) {
            throw new GeneralDatabaseHelperException(e.getMessage());
        }
    }

    private void addBasicInformationToOperationList(ArrayList<ContentProviderOperation> opList, LeaderboardInfo leaderboard, EventInfo event,
        CheckinUrlInfo checkinURL) {

        // inserting leaderboard
        ContentValues cv = new ContentValues();
        cv.put(Leaderboard.LEADERBOARD_NAME, leaderboard.name);
        cv.put(Leaderboard.LEADERBOARD_DISPLAY_NAME, leaderboard.displayName);
        cv.put(Leaderboard.LEADERBOARD_CHECKIN_DIGEST, leaderboard.checkinDigest);
        opList.add(0, ContentProviderOperation.newInsert(Leaderboard.CONTENT_URI).withValues(cv).build());

        // now insert event
        cv.clear();
        cv.put(Event.EVENT_ID, event.id);
        cv.put(Event.EVENT_NAME, event.name);
        cv.put(Event.EVENT_DATE_START, event.startMillis);
        cv.put(Event.EVENT_DATE_END, event.endMillis);
        cv.put(Event.EVENT_SERVER, event.server);
        cv.put(Event.EVENT_IMAGE_URL, event.imageUrl);
        cv.put(Event.EVENT_CHECKIN_DIGEST, event.checkinDigest);
        opList.add(ContentProviderOperation.newInsert(Event.CONTENT_URI).withValues(cv).build());

        // checkin url
        cv.clear();
        cv.put(Checkin.CHECKIN_URI_VALUE, checkinURL.urlString);
        cv.put(Checkin.CHECKIN_URI_CHECKIN_DIGEST, checkinURL.checkinDigest);
        cv.put(Checkin.CHECKIN_TYPE, checkinURL.type);
        opList.add(ContentProviderOperation.newInsert(Checkin.CONTENT_URI).withValues(cv).build());
    }

    /**
     * Return true if the combination of event, leaderboard and competitor does not exist in the DB. (based on the
     * digest of the checkin- url obtained from the QR-code.)
     *
     * @param checkinDigest
     *            SHA-256 digest of QR-code string
     * @return combination available or not
     */
    public boolean eventLeaderboardCompetitorCombinationAvailable(Context context, String checkinDigest) {
        ContentResolver cr = context.getContentResolver();
        String sel = AnalyticsDatabase.Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = ? "
            + "AND " + AnalyticsDatabase.Tables.COMPETITORS + "." + Competitor.COMPETITOR_CHECKIN_DIGEST + " = ? "
            + "AND " + AnalyticsDatabase.Tables.EVENTS + "." + Event.EVENT_CHECKIN_DIGEST + " = ?";
        Cursor cursor = cr.query(AnalyticsContract.EventLeaderboardCompetitorJoined.CONTENT_URI, null, sel, new String[] { checkinDigest,
            checkinDigest, checkinDigest }, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count == 0;
    }

    /**
     * Return true if the combination of event, leaderboard and mark does not exist in the DB. (based on the
     * digest of the checkin- url obtained from the QR-code.)
     *
     * @param checkinDigest
     *            SHA-256 digest of QR-code string
     * @return combination available or not
     */
    public boolean eventLeaderboardMarkCombinationAvailable(Context context, String checkinDigest) {
        ContentResolver cr = context.getContentResolver();
        String sel = AnalyticsDatabase.Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = ? "
            + "AND " + AnalyticsDatabase.Tables.MARKS + "." + AnalyticsContract.Mark.MARK_CHECKIN_DIGEST + " = ? "
            + "AND " + AnalyticsDatabase.Tables.EVENTS + "." + Event.EVENT_CHECKIN_DIGEST + " = ?";
        Cursor cursor = cr.query(AnalyticsContract.EventLeaderboardMarkJoined.CONTENT_URI, null, sel, new String[] { checkinDigest,
            checkinDigest, checkinDigest }, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count == 0;
    }

    public class GeneralDatabaseHelperException extends Exception {
        private static final long serialVersionUID = 4333494334720305541L;

        public GeneralDatabaseHelperException(String message) {
            super(message);
        }
    }

}
