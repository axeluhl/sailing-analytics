package com.sap.sailing.android.tracking.app.utils;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventLeaderboardCompetitorJoined;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.CheckinUri;
import com.sap.sailing.android.tracking.app.valueobjects.CompetitorInfo;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;

import java.util.ArrayList;

public class DatabaseHelper {

    private final static String TAG = DatabaseHelper.class.getName();

    protected static DatabaseHelper mInstance;

    public static synchronized DatabaseHelper getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseHelper();
        }

        return mInstance;
    }

    public long getEventRowIdForCheckinDigest(Context context, String checkinDigest) {
        int result = 0;

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Event.CONTENT_URI, null, Event.EVENT_CHECKIN_DIGEST + " = \"" + checkinDigest + "\"",
                null, null);
        cursor.moveToFirst();
        result = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        cursor.close();
        return result;
    }

    // public EventInfo getEventInfoWithLeaderboard(Context context, String eventId) {
    // EventInfo result = new EventInfo();
    //
    // ContentResolver cr = context.getContentResolver();
    // String projectionStr = "events._id,leaderboards.leaderboard_name,events.event_name";
    // String[] projection = projectionStr.split(",");
    // Cursor cursor = cr.query(LeaderboardsEventsJoined.CONTENT_URI, projection, "events.event_id = \"" + eventId +
    // "\"", null, null);
    // if (cursor.moveToFirst())
    // {
    // result.name = cursor.getString(cursor.getColumnIndex("event_name"));
    // result.leaderboardName = cursor.getString(cursor.getColumnIndex("leaderboard_name"));
    // }
    //
    // cursor.close();
    // return result;
    // }

    public EventInfo getEventInfoWithLeaderboardAndCompetitor(Context context, String checkinDigest) {
        EventInfo result = new EventInfo();

        ContentResolver cr = context.getContentResolver();
        String projectionStr = "events._id ,leaderboards.leaderboard_name, events.event_id,"
                + " events.event_name, competitors.competitor_id";
        String[] projection = projectionStr.split(",");
        Cursor cursor = cr.query(EventLeaderboardCompetitorJoined.CONTENT_URI, projection, "events."
                + Event.EVENT_CHECKIN_DIGEST + " = \"" + checkinDigest + "\"", null, null);
        if (cursor.moveToFirst()) {
            result.name = cursor.getString(cursor.getColumnIndex("event_name"));
            result.leaderboardName = cursor.getString(cursor.getColumnIndex("leaderboard_name"));
            result.competitorId = cursor.getString(cursor.getColumnIndex("competitor_id"));
            result.id = cursor.getString(cursor.getColumnIndex("event_id"));
        }

        cursor.close();
        return result;
    }

    // public EventInfo getEventInfoWithLeaderboardAndCompetitor(Context context, String eventId) {
    // EventInfo result = new EventInfo();
    //
    // ContentResolver cr = context.getContentResolver();
    // String projectionStr = "events._id,leaderboards.leaderboard_name,events.event_name, competitors.competitor_id";
    // String[] projection = projectionStr.split(",");
    // Cursor cursor = cr.query(EventLeaderboardCompetitorJoined.CONTENT_URI, projection, "events.event_id = \"" +
    // eventId + "\"", null, null);
    // if (cursor.moveToFirst())
    // {
    // result.name = cursor.getString(cursor.getColumnIndex("event_name"));
    // result.leaderboardName = cursor.getString(cursor.getColumnIndex("leaderboard_name"));
    // result.competitorId = cursor.getString(cursor.getColumnIndex("competitor_id"));
    // }
    //
    // cursor.close();
    // return result;
    // }
    //

    public EventInfo getEventInfo(Context context, String checkinDigest) {
        EventInfo event = new EventInfo();
        event.checkinDigest = checkinDigest;

        Cursor cursor = context.getContentResolver().query(Event.CONTENT_URI, null,
                Event.EVENT_CHECKIN_DIGEST + " = \"" + checkinDigest + "\"", null, null);

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
        return event;
    }

    // public CompetitorInfo getCompetitor(Context context, String competitorId)
    // {
    // CompetitorInfo competitor = new CompetitorInfo();
    // competitor.id = competitorId;
    //
    // Cursor cursor = context.getContentResolver().query(Competitor.CONTENT_URI, null, "competitor_id = \"" +
    // competitorId + "\"", null, null);
    // if (cursor.moveToFirst()) {
    // competitor.name = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_DISPLAY_NAME));
    // competitor.countryCode = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_COUNTRY_CODE));
    // competitor.sailId = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_SAIL_ID));
    // competitor.rowId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
    // }
    //
    // cursor.close();
    // return competitor;
    // }

    public CompetitorInfo getCompetitor(Context context, String checkinDigest) {
        CompetitorInfo competitor = new CompetitorInfo();
        competitor.checkinDigest = checkinDigest;

        Cursor cursor = context.getContentResolver().query(Competitor.CONTENT_URI, null,
                Competitor.COMPETITOR_CHECKIN_DIGEST + " = \"" + checkinDigest + "\"", null, null);
        if (cursor.moveToFirst()) {
            competitor.name = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_DISPLAY_NAME));
            competitor.countryCode = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_COUNTRY_CODE));
            competitor.sailId = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_SAIL_ID));
            competitor.rowId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            competitor.id = cursor.getString(cursor.getColumnIndex(Competitor.COMPETITOR_ID));
        }

        cursor.close();
        return competitor;
    }

    // public LeaderboardInfo getLeaderboard(Context context, String leaderboardName)
    // {
    // LeaderboardInfo leaderboard = new LeaderboardInfo();
    // leaderboard.name = leaderboardName;
    //
    // Cursor lc = context.getContentResolver().query(Leaderboard.CONTENT_URI, null, "leaderboard_name = \"" +
    // leaderboardName + "\"", null, null);
    // if (lc.moveToFirst()) {
    // leaderboard.rowId = lc.getInt(lc.getColumnIndex(BaseColumns._ID));
    // leaderboard.checkinDigest = lc.getString(lc.getColumnIndex(Leaderboard.LEADERBOARD_CHECKIN_DIGEST));
    // }
    //
    // lc.close();
    //
    // return leaderboard;
    // }

    public LeaderboardInfo getLeaderboard(Context context, String checkinDigest) {
        LeaderboardInfo leaderboard = new LeaderboardInfo();
        leaderboard.checkinDigest = checkinDigest;

        Cursor lc = context.getContentResolver().query(Leaderboard.CONTENT_URI, null,
                Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = \"" + checkinDigest + "\"", null, null);
        if (lc.moveToFirst()) {
            leaderboard.rowId = lc.getInt(lc.getColumnIndex(BaseColumns._ID));
            leaderboard.name = lc.getString(lc.getColumnIndex(Leaderboard.LEADERBOARD_NAME));
        }

        lc.close();

        return leaderboard;
    }

    public CheckinUrlInfo getCheckinUrl(Context context, String checkinDigest){
        CheckinUrlInfo checkinUrlInfo = new CheckinUrlInfo();
        checkinUrlInfo.checkinDigest = checkinDigest;

        Cursor uc = context.getContentResolver().query(CheckinUri.CONTENT_URI, null,
                CheckinUri.CHECKIN_URI_CHECKIN_DIGEST + " = \"" + checkinDigest + "\"", null, null);
        if (uc.moveToFirst()) {
            checkinUrlInfo.rowId = uc.getInt(uc.getColumnIndex(BaseColumns._ID));
            checkinUrlInfo.urlString = uc.getString(uc.getColumnIndex(CheckinUri.CHECKIN_URI_VALUE));
        }

        uc.close();

        return checkinUrlInfo;
    }

    // public void deleteRegattaFromDatabase(Context context, String eventId, String competitorId, String
    // leaderboardName)
    // {
    // ContentResolver cr = context.getContentResolver();
    //
    // int d1 = cr.delete(Event.CONTENT_URI, Event.EVENT_ID + " = \"" + eventId + "\"", null);
    // int d2 = cr.delete(Competitor.CONTENT_URI, Competitor.COMPETITOR_ID + " = \"" + competitorId + "\"", null);
    // int d3 = cr.delete(Leaderboard.CONTENT_URI, Leaderboard.LEADERBOARD_NAME + " = \"" + leaderboardName + "\"",
    // null);
    //
    // if (BuildConfig.DEBUG)
    // {
    // ExLog.i(context, TAG, "Checkout, number of events deleted: " + d1);
    // ExLog.i(context, TAG, "Checkout, number of competitors deleted: " + d2);
    // ExLog.i(context, TAG, "Checkout, number of leaderbards deleted: " + d3);
    // }
    // }

    public void deleteRegattaFromDatabase(Context context, String checkinDigest) {
        ContentResolver cr = context.getContentResolver();

        int d1 = cr.delete(Event.CONTENT_URI, Event.EVENT_CHECKIN_DIGEST + " = \"" + checkinDigest + "\"", null);
        int d2 = cr.delete(Competitor.CONTENT_URI, Competitor.COMPETITOR_CHECKIN_DIGEST + " = \"" + checkinDigest
                + "\"", null);
        int d3 = cr.delete(Leaderboard.CONTENT_URI, Leaderboard.LEADERBOARD_CHECKIN_DIGEST + " = \"" + checkinDigest
                + "\"", null);
        int d4 = cr.delete(CheckinUri.CONTENT_URI, CheckinUri.CHECKIN_URI_CHECKIN_DIGEST + " = \"" + checkinDigest
                + "\"", null);

        if (BuildConfig.DEBUG) {
            ExLog.i(context, TAG, "Checkout, number of events deleted: " + d1);
            ExLog.i(context, TAG, "Checkout, number of competitors deleted: " + d2);
            ExLog.i(context, TAG, "Checkout, number of leaderbards deleted: " + d3);
            ExLog.i(context, TAG, "Checkout, number of checkinurls deleted: " + d4);
        }
    }

    /**
     * When checking in, store info on the event, the competitor and the leaderboard in the database.
     * 
     * @param context
     * @param event
     * @param competitor
     * @param leaderboard
     * @return success or failure
     * @throws GeneralDatabaseHelperException
     * @throws OperationApplicationException
     * @throws RemoteException
     */
    public void storeCheckinRow(Context context, EventInfo event, CompetitorInfo competitor,
                                LeaderboardInfo leaderboard, CheckinUrlInfo checkinURL)
            throws GeneralDatabaseHelperException {

        // inserting leaderboard first

        ContentResolver cr = context.getContentResolver();

        ContentValues clv = new ContentValues();
        clv.put(Leaderboard.LEADERBOARD_NAME, leaderboard.name);
        clv.put(Leaderboard.LEADERBOARD_CHECKIN_DIGEST, leaderboard.checkinDigest);
        cr.insert(Leaderboard.CONTENT_URI, clv);

        // now insert event

        ArrayList<ContentProviderOperation> opList = new ArrayList<ContentProviderOperation>();

        ContentValues cev = new ContentValues();
        cev.put(Event.EVENT_ID, event.id);
        cev.put(Event.EVENT_NAME, event.name);
        cev.put(Event.EVENT_DATE_START, event.startMillis);
        cev.put(Event.EVENT_DATE_END, event.endMillis);
        cev.put(Event.EVENT_SERVER, event.server);
        cev.put(Event.EVENT_IMAGE_URL, event.imageUrl);
        cev.put(Event.EVENT_CHECKIN_DIGEST, event.checkinDigest);

        opList.add(ContentProviderOperation.newInsert(Event.CONTENT_URI).withValues(cev).build());

        // competitor

        ContentValues ccv = new ContentValues();

        ccv.put(Competitor.COMPETITOR_COUNTRY_CODE, competitor.countryCode);
        ccv.put(Competitor.COMPETITOR_DISPLAY_NAME, competitor.name);
        ccv.put(Competitor.COMPETITOR_ID, competitor.id);
        ccv.put(Competitor.COMPETITOR_NATIONALITY, competitor.nationality);
        ccv.put(Competitor.COMPETITOR_SAIL_ID, competitor.sailId);
        ccv.put(Competitor.COMPETITOR_CHECKIN_DIGEST, competitor.checkinDigest);

        opList.add(ContentProviderOperation.newInsert(Competitor.CONTENT_URI).withValues(ccv).build());

        // checkin url

        ContentValues ccuv = new ContentValues();

        ccuv.put(CheckinUri.CHECKIN_URI_VALUE, checkinURL.urlString);
        ccuv.put(CheckinUri.CHECKIN_URI_CHECKIN_DIGEST, checkinURL.checkinDigest);
        cr.insert(CheckinUri.CONTENT_URI, ccuv);

        opList.add(ContentProviderOperation.newInsert(CheckinUri.CONTENT_URI).withValues(ccuv).build());

        try {
            cr.applyBatch(AnalyticsContract.CONTENT_AUTHORITY, opList);
        } catch (RemoteException e) {
            throw new GeneralDatabaseHelperException(e.getMessage());
        } catch (OperationApplicationException e) {
            throw new GeneralDatabaseHelperException(e.getMessage());
        }
    }

    /**
     * Return true if the combination of event, leaderboard and competitor does not exist in the DB. (based on the
     * digest of the checkin- url obtained from the QR-code.)
     * 
     * @param checkinDigest
     *            SHA-256 digest of QR-code string
     * @param leaderboardName
     * @param competitorId
     * @return
     */
    public boolean eventLeaderboardCompetitorCombnationAvailable(Context context, String checkinDigest) {

        ContentResolver cr = context.getContentResolver();
        String sel = "leaderboards.leaderboard_checkin_digest = \"" + checkinDigest
                + "\" AND competitors.competitor_checkin_digest = \"" + checkinDigest
                + "\" AND events.event_checkin_digest = \"" + checkinDigest + "\"";

        Cursor cursor = cr.query(AnalyticsContract.EventLeaderboardCompetitorJoined.CONTENT_URI, null, sel, null, null);

        int count = cursor.getCount();

        cursor.close();
        return count == 0;
    }

    public class GeneralDatabaseHelperException extends Exception {
        private static final long serialVersionUID = 4333494334720305541L;

        public GeneralDatabaseHelperException(String message) {
            super(message);
        }
    }

}
