package com.sap.sailing.android.tracking.app.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Checkin;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventLeaderboardCompetitorJoined;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.tracking.app.provider.AnalyticsDatabase;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.android.tracking.app.valueobjects.MarkInfo;
import com.sap.sailing.domain.base.Mark;

public class DatabaseHelper {

    private final static String TAG = DatabaseHelper.class.getName();

    protected static DatabaseHelper mInstance;

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

    public long getEventRowIdForCheckinDigest(Context context, String checkinDigest) {
        int result = 0;

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Event.CONTENT_URI, null, Event.EVENT_CHECKIN_DIGEST + " = ?",
                new String[] { checkinDigest }, null);
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            cursor.close();
        }
        return result;
    }

    public EventInfo getEventInfoWithLeaderboardAndCompetitor(Context context, String checkinDigest) {
        EventInfo result = new EventInfo();

        ContentResolver cr = context.getContentResolver();
        String projectionStr = AnalyticsDatabase.Tables.EVENTS + "." + Event._ID + ","
            + AnalyticsDatabase.Tables.LEADERBOARDS + "." + Leaderboard.LEADERBOARD_NAME + ","
            + AnalyticsDatabase.Tables.EVENTS + "." + Event.EVENT_ID + ","
            + AnalyticsDatabase.Tables.EVENTS + "." + Event.EVENT_NAME + ","
            + AnalyticsDatabase.Tables.COMPETITORS + "." + Competitor.COMPETITOR_ID;
        String[] projection = projectionStr.split(",");
        Cursor cursor = cr.query(EventLeaderboardCompetitorJoined.CONTENT_URI, projection,
            AnalyticsDatabase.Tables.EVENTS + "." + Event.EVENT_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result.name = cursor.getString(cursor.getColumnIndex(Event.EVENT_NAME));
                result.leaderboardName = cursor.getString(cursor.getColumnIndex(Leaderboard.LEADERBOARD_NAME));
                result.competitorId = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_ID));
                result.id = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID));
            }

            cursor.close();
        }
        return result;
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

        Cursor lc = context.getContentResolver().query(Leaderboard.CONTENT_URI, null,
                Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest }, null);
        if (lc != null) {
            if (lc.moveToFirst()) {
                leaderboard.rowId = lc.getInt(lc.getColumnIndex(BaseColumns._ID));
                leaderboard.name = lc.getString(lc.getColumnIndex(Leaderboard.LEADERBOARD_NAME));
            }

            lc.close();
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

    public void deleteRegattaFromDatabase(Context context, String checkinDigest) {
        ContentResolver cr = context.getContentResolver();

        int d1 = cr.delete(Event.CONTENT_URI, Event.EVENT_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d2 = cr.delete(Competitor.CONTENT_URI, Competitor.COMPETITOR_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d3 = cr.delete(Leaderboard.CONTENT_URI, Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d4 = cr.delete(Checkin.CONTENT_URI, Checkin.CHECKIN_URI_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });
        int d5 = cr.delete(AnalyticsContract.Mark.CONTENT_URI, AnalyticsContract.Mark.MARK_CHECKIN_DIGEST + " = ?", new String[] { checkinDigest });

        if (BuildConfig.DEBUG) {
            ExLog.i(context, TAG, "Checkout, number of events deleted: " + d1);
            ExLog.i(context, TAG, "Checkout, number of competitors deleted: " + d2);
            ExLog.i(context, TAG, "Checkout, number of leaderboards deleted: " + d3);
            ExLog.i(context, TAG, "Checkout, number of checkinurls deleted: " + d4);
            ExLog.i(context, TAG, "Checkout, number of marks deleted: " + d5);
        }
    }

    /**
     * When checking in, store info on the event, the competitor and the leaderboard in the database.
     *
     * @param context android context
     * @param event event to be stored
     * @param competitor competitor to be stored
     * @param leaderboard leaderboard to be stored
     * @return success or failure
     * @throws GeneralDatabaseHelperException
     */
    public void storeCompetitorCheckinRow(Context context, EventInfo event, CompetitorInfo competitor,
            LeaderboardInfo leaderboard, CheckinUrlInfo checkinURL) throws GeneralDatabaseHelperException {

        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> opList = new ArrayList<>();

        // competitor

        ContentValues ccv = new ContentValues();

        ccv.put(Competitor.COMPETITOR_COUNTRY_CODE, competitor.countryCode);
        ccv.put(Competitor.COMPETITOR_DISPLAY_NAME, competitor.name);
        ccv.put(Competitor.COMPETITOR_ID, competitor.id);
        ccv.put(Competitor.COMPETITOR_NATIONALITY, competitor.nationality);
        ccv.put(Competitor.COMPETITOR_SAIL_ID, competitor.sailId);
        ccv.put(Competitor.COMPETITOR_CHECKIN_DIGEST, competitor.checkinDigest);

        opList.add(ContentProviderOperation.newInsert(Competitor.CONTENT_URI).withValues(ccv).build());
        storeBasicInformation(leaderboard, event, checkinURL, opList, contentResolver);

        try {
            contentResolver.applyBatch(AnalyticsContract.CONTENT_AUTHORITY, opList);
        } catch (RemoteException| OperationApplicationException e) {
            throw new GeneralDatabaseHelperException(e.getMessage());
        }
    }

    public void storeMarkCheckinRow(Context context, EventInfo event, Mark mark,
        LeaderboardInfo leaderboard, CheckinUrlInfo checkinURL) throws GeneralDatabaseHelperException {

        // Store Mark information
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> opList = new ArrayList<>();
        ContentValues markValues = new ContentValues();
        markValues.put(AnalyticsContract.Mark.MARK_ID, mark.getId().toString());
        markValues.put(AnalyticsContract.Mark.MARK_NAME, mark.getName());
        markValues.put(AnalyticsContract.Mark.MARK_CHECKIN_DIGEST, checkinURL.checkinDigest);
        opList.add(ContentProviderOperation.newInsert(AnalyticsContract.Mark.CONTENT_URI).withValues(markValues).build());

        storeBasicInformation(leaderboard, event, checkinURL, opList, contentResolver);

        try {
            contentResolver.applyBatch(AnalyticsContract.CONTENT_AUTHORITY, opList);
        } catch (RemoteException| OperationApplicationException e) {
            throw new GeneralDatabaseHelperException(e.getMessage());
        }
    }

    private void storeBasicInformation(LeaderboardInfo leaderboard, EventInfo event, CheckinUrlInfo checkinURL,
        ArrayList<ContentProviderOperation> opList, ContentResolver contentResolver) {

        // inserting leaderboard first

        ContentValues clv = new ContentValues();
        clv.put(Leaderboard.LEADERBOARD_NAME, leaderboard.name);
        clv.put(Leaderboard.LEADERBOARD_CHECKIN_DIGEST, leaderboard.checkinDigest);
        contentResolver.insert(Leaderboard.CONTENT_URI, clv);

        // now insert event

        ContentValues cev = new ContentValues();
        cev.put(Event.EVENT_ID, event.id);
        cev.put(Event.EVENT_NAME, event.name);
        cev.put(Event.EVENT_DATE_START, event.startMillis);
        cev.put(Event.EVENT_DATE_END, event.endMillis);
        cev.put(Event.EVENT_SERVER, event.server);
        cev.put(Event.EVENT_IMAGE_URL, event.imageUrl);
        cev.put(Event.EVENT_CHECKIN_DIGEST, event.checkinDigest);

        opList.add(ContentProviderOperation.newInsert(Event.CONTENT_URI).withValues(cev).build());

        // checkin url

        ContentValues ccuv = new ContentValues();

        ccuv.put(Checkin.CHECKIN_URI_VALUE, checkinURL.urlString);
        ccuv.put(Checkin.CHECKIN_URI_CHECKIN_DIGEST, checkinURL.checkinDigest);
        ccuv.put(Checkin.CHECKIN_TYPE, checkinURL.type);

        opList.add(ContentProviderOperation.newInsert(Checkin.CONTENT_URI).withValues(ccuv).build());
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
